package org.dreamwork.tools.sherpa.wrapper.examples;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.*;
import java.net.Socket;

import static org.dreamwork.tools.asr.JavaAudioHelper.createAudioFormat;
import static org.dreamwork.tools.asr.JavaAudioHelper.initMic;

public class TestMicClient {
    private final String server;
    private final int port;
    private final AudioFormat format = createAudioFormat ();

    private transient boolean running = true;
    private transient BufferedReader reader;
    private Thread thread;

    private transient TargetDataLine recorder;

    public TestMicClient (String server, int port) {
        this.server = server;
        this.port   = port;
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
                System.out.println ("您现在可以开始说话了");
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

                    if ("再见".equals (line)) {
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
        // $0 -s <server-host>|--server=<server-host> [-p <server-port>|--port=<server-port>] [-h|--help]
        String server = null, s_port = null;
        for (int i = 0; i < args.length; i ++) {
            String p = args[i];
            if ("-s".equals (p)) {
                i ++;
                server = args[i];
            } else if (p.startsWith ("--server=")) {
                server = p.substring ("--server=".length ());
            } else if ("-p".equals (p)) {
                i ++;
                s_port = args [i];
            } else if (p.startsWith ("--port=")) {
                s_port = p.substring ("--port=".length ());
            } else if ("-h".equals (p) || "--help".equals (p)) {
                showHelp ();
            }
        }

        if (server == null || server.isEmpty ()) {
            showHelp ();
        }

        int port = 56789;
        if (s_port != null && !s_port.isEmpty ()) {
            try {
                port = Integer.parseInt (s_port);
            } catch (Exception ex) {
                showHelp ();
            }
        }

        new TestMicClient (server, port).record ();
    }

    private static void showHelp () {
        System.out.println ("TestMicClient [options]");
        System.out.println ("    -h                 --help                   Shows this help list and exit.");
        System.out.println ("    -s <server-host>   --server=<server-host>   The address of the Recognize Server");
        System.out.println ("    -p <server-port>   --port=<server-port>     The port which the Recognize Server listened on");
        System.exit (0);
    }
}
