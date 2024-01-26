package org.dreamwork.tools.sherpa.wrapper.examples;

import com.k2fsa.sherpa.ncnn.SherpaNcnn;
import org.dreamwork.tools.sherpa.wrapper.SherpaHelper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static org.dreamwork.tools.asr.JavaAudioHelper.createAudioFormat;
import static org.dreamwork.tools.asr.JavaAudioHelper.initMic;

public class MicTest {
    // MicTest [-m <model.dir>] [--model-dir=<model.dir>]
    public static void main (String[] args) throws Exception {
/*
        String modelDir = null;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i ++) {
                String option = args[i];
                if ("-m".equals (option) && args.length > i + 1) {
                    modelDir = args [i + 1];
                    break;
                } else if (option.startsWith ("--model-dir=")) {
                    modelDir = option.substring ("--model-dir=".length ());
                    break;
                }
            }
        }
        if (modelDir == null || modelDir.isEmpty ()) {
            System.err.println ("model dir does not set, using the default location: " + new File (".").getAbsolutePath ());
        } else {
            Path path = Paths.get (modelDir);
            if (Files.notExists (path)) {
                System.err.println ("location " + path + " not exists.");
                System.exit (-1);

                return;
            } else if (!Files.isDirectory (path)) {
                System.err.println ("location " + path.toRealPath () + " is not a directory.");
                System.exit (-1);

                return;
            } else if (!Files.isReadable (path)) {
                System.err.println ("directory " + path.toRealPath () + " is not readable.");
                System.exit (-1);

                return;
            } else {
                String realPath = path.toRealPath ().toString ();
                System.out.println ("using model directory: " + realPath);
                System.setProperty (ModelConfig.MODEL_DIR_KEY, realPath);
            }
        }
*/
        SherpaHelper.initNcnnModel (args);
        MicTest test = new MicTest ();
        test.recognize ();
    }

    private final SherpaNcnn sherpa;

    public MicTest () {
        sherpa = SherpaHelper.initRecognizer (6);
    }

    public void recognize () throws InterruptedException {
        final AudioFormat format = createAudioFormat ();
        try (TargetDataLine line = initMic (format)) {
            if (line != null) {
                System.out.println ("line.getBufferSize () = " + line.getBufferSize ());
                System.out.println ("format.getFrameSize () = " + format.getFrameSize ());
                int size = line.getBufferSize () / 8 * format.getFrameSize (), length;
                System.out.println ("size = " + size);
                byte[] buff = new byte[size];

                line.start ();  // start to capture mic data
                System.out.println ("请开始你的表演...");
                while ((length = line.read (buff, 0, size)) >= 0) {
                    ByteBuffer bb = ByteBuffer.wrap (buff);
                    bb.order (ByteOrder.LITTLE_ENDIAN);
                    ShortBuffer samples = bb.asShortBuffer ();
                    short[] shorts = new short[samples.capacity ()];
                    float[] floats = new float[samples.capacity ()];
                    samples.get (shorts);
                    for (int i = 0; i < floats.length; i ++) {
                        floats[i] = shorts[i] / 32768f;
                    }

                    sherpa.acceptSamples (floats);
                    while (sherpa.isReady ()) {
                        sherpa.decode ();
                    }

                    if (sherpa.isEndpoint ()) {
                        String text = sherpa.getText ();
                        if (text != null && !text.isEmpty ()) {
                            System.out.println (text);
                        }

                        sherpa.reset (false);
                        System.out.println ("the model reset!!!");

                        if ("再见".equals (text)) {
                            System.out.println ("好的，3秒后将停止程序");

                            sherpa.close ();
                            Thread.sleep (3000);
                            System.exit (0);
                        }
                    }
                }
            }
        }
    }
}