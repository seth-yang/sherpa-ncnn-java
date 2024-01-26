package org.dreamwork.tools.sherpa.wrapper.examples;

import com.k2fsa.sherpa.ncnn.SherpaNcnn;
import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.tools.sherpa.wrapper.SherpaConfig;
import org.dreamwork.tools.sherpa.wrapper.SherpaHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DecodeFileDemo {
    public static void main (String[] args) throws IOException {
        ArgumentParser parser = CliHelper.getArgumentsParser ("decode-file-demo.json");
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

        System.out.println ("recognizing file " + path + " ...");
        SherpaConfig conf = CliHelper.convert (parser);

        try (SherpaNcnn sherpa = SherpaHelper.initRecognizer (conf)) {
            for (int i = 0; i < 10; i++) {
                System.out.printf ("round %02d%n", i + 1);
                decode (sherpa, path);
                System.out.println ();
            }
        }
    }

    private static void decode (SherpaNcnn sherpa, Path path) throws IOException {
        try (InputStream in = Files.newInputStream (path)) {
            final int N = 6400;
            byte[] raw = new byte[N];
            float[] samples = new float[N / 2];
            long start = System.currentTimeMillis ();
            while (in.read (raw, 0, N) > 0) {
                for (int i = 0; i < N; i += 2) {
                    short value = (short) (((raw[i + 1] & 0xff) << 8) | (raw[i] & 0xff));
                    samples[i / 2] = value / 32768.0f;
                }
                sherpa.acceptSamples (samples);
                while (sherpa.isReady ()) {
                    sherpa.decode ();
                }
            }
            float[] tail = new float[4800];
            sherpa.acceptSamples (tail);
            sherpa.inputFinished ();
            while (sherpa.isReady ()) {
                sherpa.decode ();
            }
            String text = sherpa.getText ();
            if (text != null && !text.isEmpty ()) {
                System.out.println (text);
            }
            System.out.printf ("recognize %s takes %d ms.%n", path, System.currentTimeMillis () - start);
        } finally {
            sherpa.reset (false);
        }
    }
}