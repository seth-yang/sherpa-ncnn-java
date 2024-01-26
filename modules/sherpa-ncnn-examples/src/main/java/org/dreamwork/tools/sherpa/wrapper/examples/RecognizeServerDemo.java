package org.dreamwork.tools.sherpa.wrapper.examples;

import com.k2fsa.sherpa.ncnn.SherpaNcnn;
import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.tools.sherpa.wrapper.SherpaConfig;
import org.dreamwork.tools.sherpa.wrapper.SherpaHelper;
import org.dreamwork.tools.sherpa.wrapper.examples.utils.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecognizeServerDemo {
    final ExecutorService executor = Executors.newCachedThreadPool ();
    final ServerSocket server;

    transient boolean running = true, watching;
    transient long lastReceivedAt;
    SherpaNcnn recognizer;
    private final Logger logger = new Logger ();
    private final SherpaConfig conf;

    public static void main (String... args) throws Exception {
        ArgumentParser parser = CliHelper.getArgumentsParser ("recognize-server.json");
        if (parser == null) {
            System.err.println ("cannot parse command line arguments");
            System.exit (-1);
        }
        // parse the command line arguments
        parser.parse (args);

        // check if necessary parameters exist.
        if (CliHelper.isRequiredArgMissing (parser)) {
            System.out.println ("Usage: java -cp $CLASSPATH DecodeFileDemo options, ");
            System.out.println ("which options can be: ");
            parser.showHelp ();
            System.exit (-2);
        }

        // if -h option present, show the help list then exit.
        if (parser.isArgPresent ('h')) {
            parser.showHelp ();
            System.exit (0);
        }

        String temp = parser.getValue ('P');
        if (temp == null || temp.isEmpty ()) {
            temp = parser.getDefaultValue ('P');
        }
        int port = Integer.parseInt (temp);
        if (port <= 0 || port > 65535) {
            System.err.println ("Invalid port: " + port + ".");
            System.exit (-1);
        }

        SherpaConfig conf = CliHelper.convert (parser);
        RecognizeServerDemo server = new RecognizeServerDemo (port, conf);
        server.bind ();
    }

    public RecognizeServerDemo (int port, SherpaConfig conf) throws IOException {
        server = new ServerSocket (port, -1, InetAddress.getByName ("0.0.0.0"));
        this.conf = conf;
    }

    public void bind () {
        recognizer = SherpaHelper.initRecognizer (conf);
        executor.execute (this::listen);
//        executor.shutdown ();
        logger.info ("server listen on port: %d, ready for recognizing!", server.getLocalPort ());
    }

    private void listen () {
        while (running) {
            try (Socket socket = server.accept ()) {
                SocketAddress remote = socket.getRemoteSocketAddress ();
                logger.info ("a client connect to me from: %s", remote);
                InputStream in = socket.getInputStream ();
                OutputStream out = socket.getOutputStream ();
                PrintWriter writer = new PrintWriter (out, true);

                int b1 = in.read (), b2 = in.read ();
                int size = ((b1 & 0xff) << 8) | (b2 & 0xff), length;
                logger.info ("buff size = %d", size);
                writer.println ("::Ready::");
                out.flush ();
                logger.info ("waiting for the client feed me...");

                receive (size, in, writer);
                logger.info ("the client %s lost.", remote);
                watching = false;
            } catch (IOException ex) {
                logger.warn (ex.getMessage (), ex);
                break;
            }
        }
    }

    private void receive (int size, final InputStream in, final PrintWriter writer) throws IOException {
        byte[] buff = new byte[size];
        float[] samples = new float[size / 2];

        String old = null;
        long lastTimestamp = 0;
        int length;

        watching = true;
        // touch last receive timestamp.
        lastReceivedAt = System.currentTimeMillis ();
        executor.execute (() -> {
            while (watching) {
                if (System.currentTimeMillis () - lastReceivedAt > 500) {   // timeout 500ms.
                    logger.info ("timed out after 500 ms.");
                    watching = false;
                    // reply the recognize result, it there's a result.
                    String text = recognizer.getText ();
                    if (text != null && !text.isEmpty ()) {
                        writer.println (text);
                        writer.flush ();
                    }
                    // tell the client, i'm done 300 ms later
                    try {
                        Thread.sleep (300);
                    } catch (InterruptedException ignore) {
                        // who's care
                    }
                    writer.println ("::Finish::");
                    writer.flush ();
                    // reset the recognizer and waiting the next request
                    recognizer.reset (false);
                    break;
                } else {
                    try {
                        Thread.sleep (50);  // check every 50 ms.
                    } catch (InterruptedException ignore) {
                        // nobody cares, either
                    }
                }
            }
            logger.info ("watcher killed.");
        });

        while ((length = in.read (buff, 0, size)) > 0) {
            lastReceivedAt = System.currentTimeMillis ();
            for (int i = 0; i < length; i += 2) {
                short value = (short) (((buff[i + 1] & 0xff) << 8) | (buff[i] & 0xff));
                samples[i / 2] = value / 32768.0f;
            }
            recognizer.acceptSamples (samples);
            while (recognizer.isReady ()) {
                recognizer.decode ();
            }

            long now = System.currentTimeMillis ();
            String text = recognizer.getText ();
            if (text != null && !text.isEmpty ()) {
                if (!text.equals (old)) {
                    old = text;
                    lastTimestamp = now;
                } else {
                    if (now - lastTimestamp > 200) {
                        old = null;
                        lastTimestamp = 0;
                    }
                }
            }

            boolean endpoint = recognizer.isEndpoint ();
            if (endpoint /*in.available () == 0*/) {
                logger.info ("got a result: %s", old);
                writer.println (old);
                // tells the client, recognize done.
                writer.println ("::Finish::");
                recognizer.reset (false);
                watching = false; // kill the watcher
            }
        }
    }
}