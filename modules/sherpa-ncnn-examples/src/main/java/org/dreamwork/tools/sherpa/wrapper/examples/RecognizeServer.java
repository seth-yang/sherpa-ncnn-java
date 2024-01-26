package org.dreamwork.tools.sherpa.wrapper.examples;

import com.k2fsa.sherpa.ncnn.SherpaNcnn;
import org.dreamwork.tools.sherpa.wrapper.ModelType;
import org.dreamwork.tools.sherpa.wrapper.SherpaHelper;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class RecognizeServer {
    final ExecutorService executor = Executors.newCachedThreadPool ();
    final ServerSocket server;

    transient boolean running = true;
    SherpaNcnn recognizer;
    private final ModelType type;
    private final Logger logger = new Logger ();

    public static void main (String... args) throws Exception {
        // $0 [-m <model-dir>|--model=<model-dir>] [-p <port>|--port=<port>] [-t <model-type>|--model-type=<model-type>]
        int port = 56789;
        ModelType type = ModelType.StreamingZipFormerSmallBilingualZhEn;
        String txt_port = null, txt_type = null;

        for (int i = 0; i < args.length; i++) {
            String part = args[i];
            if ("-h".equals (part) || "--help".equals (part)) {
                System.out.println ("Usage: ");
                System.out.println ("    -m <model-dir>    --model-dir=<model-dir>        the model dir, default to ../models");
                System.out.println ("    -p <port>         --port=<port>                  the port which the server will listen on, default to 56789");
                System.out.println ("    -t <model-type>   --model-type=<model-type>      the model type which the recognizer will use. default to StreamingZipFormerSmallBilingualZhEn");
                System.out.println ("    -h                --help                         show this help list.");
                System.exit (0);
            } else if ("-p".equals (part)) {
                if (i == args.length - 1) {
                    System.err.println ("arg: port needs value. use the default value: 56789");
                } else {
                    i++;
                    txt_port = args[i];
                }
            } else if (part.startsWith ("--port=")) {
                txt_port = part.substring ("--port=".length ());
            } else if ("-t".equals (part)) {
                if (i == args.length - 1) {
                    System.err.println ("arg: type needs value. use the default value: StreamingZipFormerSmallBilingualZhEn");
                } else {
                    i++;
                    txt_type = args[i];
                }
            } else if (part.endsWith ("--model-type=")) {
                txt_type = part.substring ("--model-type=".length ());
            }

            if (txt_port != null) {
                try {
                    port = Integer.parseInt (part);
                } catch (NumberFormatException ex) {
                    System.err.println ("invalid port number: " + txt_port + ", use the default value: 56789");
                }
            }
            if (txt_type != null) {
                try {
                    type = ModelType.valueOf (txt_type);
                } catch (Exception ex) {
                    System.err.println ("unknown model type: " + txt_type + ", use the default value: StreamingZipFormerSmallBilingualZhEn");
                }
            }
        }

        SherpaHelper.initNcnnModel (args);

        RecognizeServer server = new RecognizeServer (port, type);
        server.bind ();
    }

    public RecognizeServer (int port, ModelType type) throws IOException {
        server = new ServerSocket (port, -1, InetAddress.getByName ("0.0.0.0"));
        this.type = type;
    }

    public void bind () {
        recognizer = SherpaHelper.initRecognizer (type.ordinal ());
        executor.execute (this::listen);
        executor.shutdown ();
        logger.info ("ready for recognizing!");
    }

    private void listen () {
        while (running) {
            try (Socket socket = server.accept ()) {
                logger.info ("a client connect to me from: %s", socket.getRemoteSocketAddress ());
                InputStream in = socket.getInputStream ();
                OutputStream out = socket.getOutputStream ();
                PrintWriter writer = new PrintWriter (out, true);

                int b1 = in.read (), b2 = in.read ();
                int size = ((b1 & 0xff) << 8) | (b2 & 0xff), length;
                logger.info ("buff size = %d", size);
                writer.println ("::Ready::");
                out.flush ();
                logger.info ("waiting for the client feed me...");

                byte[] buff = new byte[size];
                float[] samples = new float[size / 2];

                String old = null;
                long lastTimestamp = 0;

                while ((length = in.read (buff, 0, size)) > 0) {
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

                        boolean endpoint = recognizer.isEndpoint ();
                        if (endpoint || in.available () == 0) {
                            logger.info ("got a result: %s", old);
                            writer.println (old);
                            old = null;
                            lastTimestamp = 0;
                            recognizer.reset (false);

                            logger.info (endpoint ? "endpoint" : "no more data.");
                        }
                    }
                }
            } catch (IOException ex) {
                logger.warn (ex.getMessage (), ex);
                break;
            }
        }
    }

    private static final class Logger implements Runnable {
        final BlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<> ();
        final Thread thread;

        Logger () {
            thread = new Thread (this);
            thread.setDaemon (true);
            thread.start ();
        }

        void info (String pattern, Object... args) {
            try {
                queue.put (new MessageWrapper (pattern, args));
            } catch (InterruptedException e) {
                throw new RuntimeException (e);
            }
        }

        void warn (String message, Throwable ex) {
            StringWriter writer = new StringWriter (1024);
            PrintWriter pw = new PrintWriter (writer);
            ex.printStackTrace (pw);
            try {
                MessageWrapper w = new MessageWrapper (message);
                w.type = "error";
                queue.put (w);

                w = new MessageWrapper (writer.toString ());
                w.type = "error";
                queue.put (w);
            } catch (InterruptedException ie) {
                throw new RuntimeException (ie);
            }
        }

        @Override
        public void run () {
            SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
            while (!thread.isInterrupted ()) {
                MessageWrapper w;
                try {
                    w = queue.take ();
                } catch (InterruptedException e) {
                    throw new RuntimeException (e);
                }
                String pattern = "[%s][%s] - " + w.pattern + "%n";
                Object[] args = new Object[2 + w.args.length];
                args[0] = sdf.format (System.currentTimeMillis ());
                args[1] = w.name;
                if (w.args.length > 0) {
                    System.arraycopy (w.args, 0, args, 2, w.args.length);
                }

                if ("error".equals (w.type)) {
                    System.err.printf (pattern, args);
                } else {
                    System.out.printf (pattern, args);
                }
            }
        }
    }

    private static final class MessageWrapper {
        String pattern, name, type;
        Object[] args;

        MessageWrapper (String pattern, Object... args) {
            this.pattern = pattern;
            this.args = args;
            this.name = Thread.currentThread ().getName ();
        }
    }
}