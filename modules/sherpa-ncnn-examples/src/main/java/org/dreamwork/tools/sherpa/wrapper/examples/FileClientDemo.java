package org.dreamwork.tools.sherpa.wrapper.examples;

import org.dreamwork.cli.ArgumentParser;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileClientDemo {
    public static void main (String[] args) throws Exception {
        ArgumentParser parser = CliHelper.getArgumentsParser (
                "decode-file-demo.json",    // using -f option definition
                "recognize-client.json"     // using -p -h options definition
        );
        if (parser == null) {
            System.err.println ("cannot parse command line arguments");
            System.exit (-1);
        }
        // parse the command line arguments
        parser.parse (args);
        // check if mandatory parameters exist.
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

        // check the wave file which specified by option "-f",
        // which is defined in decode-file-demo.json
        Path path = Paths.get (parser.getValue ('f'));
        if (Files.notExists (path)) {
            System.err.println (path + " not exists.");
            System.exit (-1);
            return;
        }

        final int N = 6400;
        byte[] raw = new byte[N];
        final Object LOCKER = new byte[0];
//        for (int i = 0; i < 5; i++) {
            try (Socket socket = new Socket ("127.0.0.1", 56789)) {
                Thread receiver = getThread (socket, LOCKER);
                receiver.start ();
                new Thread (() -> {
                    try {
                        OutputStream out = socket.getOutputStream ();
                        // simple handshake: send the buffer size to server in 2-bytes.
                        out.write (N >> 8);
                        out.write (N);
                        // send the wave file data.
                        try (InputStream in = Files.newInputStream (path)) {
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
//        }
    }

    private static Thread getThread (Socket socket, Object LOCKER) throws IOException {
        InputStream in = socket.getInputStream ();
        return new Thread (() -> {
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            String line;
            try {
                while ((line = reader.readLine ()) != null) {
                    if ("::Finish::".equals (line)) {
                        reader.close ();
                        in.close ();
                        socket.close ();
                        System.out.println ("finished");
                        break;
                    } else {
                        System.out.println (line);
//                        break;
                    }
                }

                System.out.println ("receiver broken.");
            } catch (IOException ex) {
                throw new RuntimeException (ex);
            }
        });
    }
}
