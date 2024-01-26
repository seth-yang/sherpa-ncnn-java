package org.dreamwork.tools.sherpa.wrapper.examples;

import com.k2fsa.sherpa.ncnn.SherpaNcnn;
import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.tools.sherpa.wrapper.SherpaConfig;
import org.dreamwork.tools.sherpa.wrapper.SherpaHelper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import static org.dreamwork.tools.asr.JavaAudioHelper.createAudioFormat;
import static org.dreamwork.tools.asr.JavaAudioHelper.initMic;

public class MicTest {
    public static void main (String[] args) throws Exception {
        ArgumentParser parser = CliHelper.getArgumentsParser ("decode-file-demo.json");
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

        SherpaConfig conf = CliHelper.convert (parser);
        try (SherpaNcnn sherpa = SherpaHelper.initRecognizer (conf)) {
            final AudioFormat format = createAudioFormat ();

            // init the microphone
            try (TargetDataLine line = initMic (format)) {
                if (line != null) {
                    // calculate the buffer size
                    int size = line.getBufferSize () / 8 * format.getFrameSize ();
                    byte[] buff = new byte[size];
                    float[] samples = new float[size / 2];

                    line.start ();  // start to capture mic data
                    System.out.println ("I'm listening, please speak to me.");
                    System.out.println ("You can say \"再见\", \"拜拜\" or \"Goodbye\" to exit the program");
                    while (line.read (buff, 0, size) >= 0) {
                        for (int i = 0; i < buff.length; i += 2) {
                            short value = (short) (((buff[i + 1] & 0xff) << 8) | (buff[i] & 0xff));
                            samples[i / 2] = value / 32768f;
                        }

                        sherpa.acceptSamples (samples);
                        while (sherpa.isReady ()) {
                            sherpa.decode ();
                        }
                        if (sherpa.isEndpoint ()) {
                            String text = sherpa.getText ();
                            if (text != null && !text.isEmpty ()) {
                                System.out.println (text);

                                if ("再见".equals (text) ||
                                    "拜拜".equals (text) ||
                                    "Goodbye".equalsIgnoreCase (text)) {
                                    System.out.println ("好的，再见!");
                                    System.out.println ("OK, See you!");
                                    break;
                                }
                            }

                            sherpa.reset (false);
                        }
                    }
                }
            }
        }
    }
}