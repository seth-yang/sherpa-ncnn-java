package org.dreamwork.tools.sherpa.wrapper.examples;

import org.dreamwork.cli.ArgumentParser;

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
                reader = new BufferedReader (new InputStreamReader (in));

                thread = new Thread (this::receive);

                int size = recorder.getBufferSize () / 8 * format.getFrameSize (), length;
                byte[] buff = new byte[size];

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

    private void receive () {
        try {
            while (running && !thread.isInterrupted ()) {
                String line = reader.readLine ();
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
            ex.printStackTrace ();
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

        String server = "127.0.0.1", s_port = null;
        int port = 56789;
        if (parser.isArgPresent ('H')) {
            server = parser.getValue ('H');
        }
        if (parser.isArgPresent ('P')) {
            s_port = parser.getValue ('P');
            port = Integer.parseInt (s_port);
        }
        System.out.printf ("connecting to %s:%d...", server, port);

        new MicClientDemo (server, port).record ();
    }
}
