package org.dreamwork.tools.sherpa.wrapper.examples;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestClient {
    public static void main (String[] args) throws Exception {
        final int N = 6400;
        byte[] raw = new byte[N];
        Path path = Paths.get (args[0]);

        final Object LOCKER = new byte[0];

        for (int i = 0; i < 5; i++) {
            try (Socket socket = new Socket ("127.0.0.1", 56789)) {
                Thread receiver = getThread (socket, LOCKER);
                receiver.start ();
                new Thread (() -> {
                    try {
                        OutputStream out = socket.getOutputStream ();
                        out.write (N >> 8);
                        out.write (N);
/*
                        synchronized (LOCKER) {
                            try {
                                LOCKER.wait ();
                                System.out.println ("start to send data ...");
                            } catch (InterruptedException e) {
                                throw new RuntimeException (e);
                            }
                        }
*/
                        try (InputStream in = Files.newInputStream (path)) {
                            in.skip (44);
                            int length;
                            while ((length = in.read (raw, 0, N)) >= 0) {
                                out.write (raw, 0, length);
                                out.flush ();
                            }
                        }
                        System.out.println ("sender complete.");
                    } catch (IOException ex) {
                        throw new RuntimeException (ex);
                    }
                }).start ();
                receiver.join ();
            }
        }
    }

    private static Thread getThread (Socket socket, Object LOCKER) throws IOException {
        InputStream in = socket.getInputStream ();
        return new Thread (() -> {
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            String line;
            try {
                while ((line = reader.readLine ()) != null) {
                    System.out.println (line);
/*
                    if ("::Ready::".equals (line)) {
                        synchronized (LOCKER) {
                            LOCKER.notifyAll ();
                        }
                    } else {
                        System.out.println (line);
                        break;
                    }
*/
                }

                System.out.println ("receiver broken.");
            } catch (IOException ex) {
                throw new RuntimeException (ex);
            }
        });
    }
}
