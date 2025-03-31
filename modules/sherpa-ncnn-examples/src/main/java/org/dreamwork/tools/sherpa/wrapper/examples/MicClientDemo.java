package org.dreamwork.tools.sherpa.wrapper.examples;

import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.util.StringUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.*;
import java.net.Socket;

import static org.dreamwork.tools.asr.JavaAudioHelper.createAudioFormat;
import static org.dreamwork.tools.asr.JavaAudioHelper.initMic;

public class MicClientDemo {
    private final String server;
    private final int port;
    private final AudioFormat format = createAudioFormat ();

    private transient boolean running = true;
    private transient BufferedReader reader;
    private Thread thread;

    private transient TargetDataLine recorder;

    public MicClientDemo (String server, int port) {
        this.server = server;
        this.port = port;
    }

    public void record () throws IOException {
        try (Socket socket = new Socket (server, port)) {
            recorder = initMic (format);
            if (recorder != null) {
                InputStream in = socket.getInputStream ();
                OutputStream out = socket.getOutputStream ();

                int size = recorder.getBufferSize () / 8 * format.getFrameSize (), length;
                byte[] buff = new byte[size];

                // handshake
                byte[] header = {
                        (byte) 0xca, (byte) 0xfe,
                        (byte) (size >> 24), (byte) (size >> 16), (byte) (size >> 8), (byte) (size & 0xff)
                };
                out.write (header);
                out.flush ();
                System.out.printf ("packet: %s send.%n", StringUtil.format (header));

                byte[] data = new byte[3];
                int read = in.read (data);
                if (read != 3 || (data[0] & 0xff) != 0xba || (data[1] & 0xff) != 0xbe || data[2] != 0) {
                    System.err.println ("handshake failed");
                    System.err.printf ("expect 0xbabe00, but got 0x%02x%02x%02x%n", data[0], data[1], data[2]);
                } else { // handshake success
                    reader = new BufferedReader (new InputStreamReader (in));
                    thread = new Thread (this::receive);
                    thread.start ();

                    recorder.start ();
                    System.out.println ("I'm listening, please speak to me.");
                    System.out.println ("You can say \"再见\", \"拜拜\" or \"Goodbye\" to exit the program");
                    while (running && (length = recorder.read (buff, 0, size)) != -1) {
                        out.write (buff, 0, length);
                        out.flush ();
                    }
                }
            }
        }
    }

    private void receive () {
        System.out.println ("starting receiving thread ... ");
        try {
            while (running/* && !thread.isInterrupted ()*/) {
                String line = reader.readLine ();
                System.out.println ("line = " + line);
                if (line != null && !line.isEmpty ()) {
                    System.out.println (line);

                    if ("再见".equals (line) || "拜拜".equals (line)) {
                        running = false;
                        thread.interrupt ();

                        recorder.stop ();
                        recorder.close ();

                        System.out.println ("好的，再见!");
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace (System.err);
        }
    }

    public static void main (String[] args) throws IOException {
        ArgumentParser parser = CliHelper.getArgumentsParser ("recognize-server", "recognize-client");
        if (parser == null) {
            System.err.println ("cannot parse command line arguments");
            System.exit (-1);
        }
        // parse the command line arguments
        parser.parse (args);

        // if -h option present, show the help list then exit.
        if (parser.isArgPresent ('h')) {
            parser.showHelp ();
            System.exit (0);
        }

        String server = "10.247.1.31", s_port;
        int port = 56789;
        if (parser.isArgPresent ('H')) {
            server = parser.getValue ('H');
        }
        if (parser.isArgPresent ('P')) {
            s_port = parser.getValue ('P');
            port = Integer.parseInt (s_port);
        }
        System.out.printf ("connecting to %s:%d...%n", server, port);

        new MicClientDemo (server, port).record ();
    }
}