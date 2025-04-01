package org.dreamwork.tools.sherpa.wrapper;

import com.k2fsa.sherpa.ncnn.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
@SuppressWarnings ("unused")
public class SherpaHelper {
    private static int THREAD_NUMBERS = 3;

    public static final String MODEL_DIR_KEY = "sherpa.ncnn.model.dir";
    private static String MODEL_ROOT;

    public static SherpaNcnn initRecognizer (int type) {
        System.out.print ("initialing recognizer ... ");
        long start = System.currentTimeMillis ();
        FeatureExtractorConfig featConfig = new FeatureExtractorConfig (16000.0f, 80);
        ModelConfig modelConfig = getModelConfig (type, true);
        DecoderConfig decoderConfig = new DecoderConfig ("greedy_search", 4);
        RecognizerConfig recognizerConfig = new RecognizerConfig (
                featConfig, modelConfig, decoderConfig,
                true, 2.4f, 1.4f, 20f,
                "", 1.5f
        );
        SherpaNcnn sherpa = new SherpaNcnn (recognizerConfig);
        long end = System.currentTimeMillis ();
        System.out.printf ("done, it takes %.3f seconds.%n", (end - start) / 1000f);
        return sherpa;
    }

    public static SherpaNcnn initRecognizer (SherpaConfig conf) {
        System.out.println ("initialing recognizer ...");
        MODEL_ROOT = conf.basedir;
        if (conf.threads <= 0) {
            int count = Runtime.getRuntime ().availableProcessors ();
            THREAD_NUMBERS = count / 2;
            if (THREAD_NUMBERS < 1) {
                THREAD_NUMBERS = 1;
            }
        }

        FeatureExtractorConfig featConfig = new FeatureExtractorConfig (conf.sampleRate, conf.featureDim);
        ModelConfig modelConfig = getModelConfig (conf.modelName, conf.useGpu);
        DecoderConfig decoderConfig = new DecoderConfig (conf.decoderMethod, conf.activePaths);
        RecognizerConfig recognizerConfig = new RecognizerConfig (
                featConfig, modelConfig, decoderConfig,
                conf.endpointEnabled, conf.r1Silence, conf.r2Silence, conf.r3utterance,
                conf.hotWordsFile, conf.hotWordScore
        );
        return new SherpaNcnn (recognizerConfig);
    }

    public static void initNcnnModel (String... args) throws IOException {
        String modelDir = null;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if ("-m".equals (option) && args.length > i + 1) {
                    modelDir = args[i + 1];
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
                System.err.println ("location " + path + " does not exists.");
                System.exit (-1);
            } else if (!Files.isDirectory (path)) {
                System.err.println ("location " + path.toRealPath () + " is not a directory.");
                System.exit (-1);
            } else if (!Files.isReadable (path)) {
                System.err.println ("directory " + path.toRealPath () + " is not readable.");
                System.exit (-1);
            } else {
                String realPath = path.toRealPath ().toString ();
                System.out.println ("using model directory: " + realPath);
                System.setProperty (MODEL_DIR_KEY, realPath);
            }
        }
    }

    public static ModelConfig getModelConfig (int type, boolean useGPU) {
        switch (type) {
            case 0:
                return createDefaultModelConfig (useGPU);
            case 1:
                return createTransducerModelConfig (useGPU);
            case 2:
                return createBilingualZhEnModelConfig (useGPU);
            case 3:
                return createStreamingZipFormerEnModelConfig (useGPU);
            case 4:
                return createStreamingZipFormerFrModelConfig (useGPU);
            case 5:
                return createStreamingZipFormerBilingualZhEnModelConfig (useGPU);
            case 6:
                return createStreamingZipFormerSmallBilingualZhEnModelConfig (useGPU);

            default:
                return null;
        }
    }

    public static ModelConfig getModelConfig (String type, boolean useGPU) {
        return getModelConfig (ModelType.of (type), useGPU);
    }

    public static ModelConfig getModelConfig (ModelType type, boolean useGPU) {
        switch (type) {
            case Default:
                return createDefaultModelConfig (useGPU);
            case Transducer:
                return createTransducerModelConfig (useGPU);
            case BilingualZhEn:
                return createBilingualZhEnModelConfig (useGPU);
            case StreamingZipFormerEn:
                return createStreamingZipFormerEnModelConfig (useGPU);
            case StreamingZipFormerFr:
                return createStreamingZipFormerFrModelConfig (useGPU);
            case StreamingZipFormerBilingualZhEn:
                return createStreamingZipFormerBilingualZhEnModelConfig (useGPU);
            case StreamingZipFormerSmallBilingualZhEn:
                return createStreamingZipFormerSmallBilingualZhEnModelConfig (useGPU);

            default:
                return null;
        }
    }

    // type = 0
    private static ModelConfig createDefaultModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-2022-09-30", useGPU);
    }

    // type = 1
    private static ModelConfig createTransducerModelConfig (boolean useGPU) {
        String modelDir = MODEL_ROOT + "/sherpa-ncnn-conv-emformer-transducer-2022-12-06";
        ModelConfig config = new ModelConfig ();
        config.setEncoderParam (modelDir + "/encoder_jit_trace-pnnx.ncnn.int8.param");
        config.setEncoderBin (modelDir + "/encoder_jit_trace-pnnx.ncnn.int8.bin");
        config.setDecoderParam (modelDir + "/decoder_jit_trace-pnnx.ncnn.param");
        config.setDecoderBin (modelDir + "/decoder_jit_trace-pnnx.ncnn.bin");
        config.setJoinerParam (modelDir + "/joiner_jit_trace-pnnx.ncnn.int8.param");
        config.setJoinerBin (modelDir + "/joiner_jit_trace-pnnx.ncnn.int8.bin");
        config.setTokens (modelDir + "/tokens.txt");
        config.setNumThreads (THREAD_NUMBERS);
        config.setUseGPU (useGPU);

        return config;
    }

    // type = 2
    private static ModelConfig createBilingualZhEnModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-streaming-zipformer-bilingual-zh-en-2023-02-13", useGPU);
    }

    // type =3
    private static ModelConfig createStreamingZipFormerEnModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-streaming-zipformer-en-2023-02-13", useGPU);
    }

    // type = 4
    private static ModelConfig createStreamingZipFormerFrModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-streaming-zipformer-fr-2023-04-14", useGPU);
    }

    // type = 5
    private static ModelConfig createStreamingZipFormerBilingualZhEnModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-streaming-zipformer-bilingual-zh-en-2023-02-13", useGPU);
    }

    // type = 6
    private static ModelConfig createStreamingZipFormerSmallBilingualZhEnModelConfig (boolean useGPU) {
        return createModelConfig ("sherpa-ncnn-streaming-zipformer-small-bilingual-zh-en-2023-02-16", useGPU);
    }

    private static ModelConfig createModelConfig (String name, boolean useGPU) {
        ModelConfig config = new ModelConfig ();
        name = MODEL_ROOT + '/' + name;
        config.setEncoderParam (name + "/encoder_jit_trace-pnnx.ncnn.param");
        config.setEncoderBin (name + "/encoder_jit_trace-pnnx.ncnn.bin");
        config.setDecoderParam (name + "/decoder_jit_trace-pnnx.ncnn.param");
        config.setDecoderBin (name + "/decoder_jit_trace-pnnx.ncnn.bin");
        config.setJoinerParam (name + "/joiner_jit_trace-pnnx.ncnn.param");
        config.setJoinerBin (name + "/joiner_jit_trace-pnnx.ncnn.bin");
        config.setTokens (name + "/tokens.txt");
        config.setNumThreads (THREAD_NUMBERS);
        config.setUseGPU (useGPU);
        System.out.println ("asr will work on " + THREAD_NUMBERS + " threads.");
        return config;
    }

    public static SherpaConfig fromProperties (Properties props) {
        SherpaConfig c = new SherpaConfig ();
        if (props.containsKey ("sherpa.ncnn.model.dir")) {
            c.basedir = props.getProperty ("sherpa.ncnn.model.dir");
        }
        if (props.containsKey ("sherpa.ncnn.model.gpu.disabled")) {
            c.useGpu = !getBoolean (props,"sherpa.ncnn.model.gpu.disabled");
        }
        if (props.containsKey ("sherpa.ncnn.model.name")) {
            c.modelName = props.getProperty ("sherpa.ncnn.model.name");
        }
        if (props.containsKey ("sherpa.ncnn.model.threads")) {
            c.threads = getInteger (props,"sherpa.ncnn.model.threads", -1);
        }
        if (props.containsKey ("sherpa.ncnn.model.encoder.param")) {
            c.encoderParam = props.getProperty ("sherpa.ncnn.model.encoder.param");
        }
        if (props.containsKey ("sherpa.ncnn.model.encoder.bin")) {
            c.encoderBin = props.getProperty ("sherpa.ncnn.model.encoder.bin");
        }
        if (props.containsKey ("sherpa.ncnn.model.decoder.param")) {
            c.decoderParam = props.getProperty ("sherpa.ncnn.model.decoder.param");
        }
        if (props.containsKey ("sherpa.ncnn.model.decoder.bin")) {
            c.decoderBin = props.getProperty ("sherpa.ncnn.model.decoder.bin");
        }
        if (props.containsKey ("sherpa.ncnn.model.joiner.param")) {
            c.joinerParam = props.getProperty ("sherpa.ncnn.model.joiner.param");
        }
        if (props.containsKey ("sherpa.ncnn.model.joiner.bin")) {
            c.joinerBin = props.getProperty ("sherpa.ncnn.model.joiner.bin");
        }
        if (props.containsKey ("sherpa.ncnn.model.tokens")) {
            c.tokens = props.getProperty ("sherpa.ncnn.model.tokens");
        }
        if (props.containsKey ("sherpa.ncnn.feature.sample.rate")) {
            c.sampleRate = getFloat (props, "sherpa.ncnn.feature.sample.rate", 16000);
        }
        if (props.containsKey ("sherpa.ncnn.feature.dim")) {
            c.featureDim = getInteger (props, "sherpa.ncnn.feature.dim", 80);
        }
        if (props.containsKey ("sherpa.ncnn.decoder.method")) {
            c.decoderMethod = props.getProperty ("sherpa.ncnn.decoder.method");
        }
        if (props.containsKey ("sherpa.ncnn.decoder.active.paths")) {
            c.activePaths = getInteger (props, "sherpa.ncnn.decoder.active.paths", 4);
        }
        if (props.containsKey ("sherpa.ncnn.asr.endpoint.enabled")) {
            c.endpointEnabled = getBoolean (props,"sherpa.ncnn.asr.endpoint.enabled");
        }
        if (props.containsKey ("sherpa.ncnn.asr.rule1.min.trailing.silence")) {
            c.r1Silence = getFloat (props, "sherpa.ncnn.asr.rule1.min.trailing.silence", 2.4f);
        }
        if (props.containsKey ("sherpa.ncnn.asr.rule2.min.trailing.silence")) {
            c.r2Silence = getFloat (props, "sherpa.ncnn.asr.rule2.min.trailing.silence", 1.4f);
        }
        if (props.containsKey ("sherpa.ncnn.asr.rule3.min.utterance.length")) {
            c.r3utterance = getFloat (props, "sherpa.ncnn.asr.rule3.min.utterance.length", 20);
        }
        if (props.containsKey ("sherpa.ncnn.asr.hot-words.file")) {
            c.hotWordsFile = props.getProperty ("sherpa.ncnn.asr.hot-words.file");
        }
        if (props.containsKey ("sherpa.ncnn.asr.hot-words.score")) {
            c.hotWordScore = getFloat (props, "sherpa.ncnn.asr.hot-words.score", 1.5f);
        }

        return c;
    }

    private static int getInteger (Properties props, String key, int defaultValue) {
        String property = props.getProperty (key);
        if (isEmpty (property)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt (property.trim ());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static float getFloat (Properties props, String key, float defaultValue) {
        String property = props.getProperty (key);
        if (isEmpty (property)) {
            return defaultValue;
        }

        try {
            return Float.parseFloat (property.trim ());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static boolean getBoolean (Properties props, String key) {
        String property = props.getProperty (key);
        if (isEmpty (property)) {
            return false;
        }

        try {
            return Boolean.parseBoolean (property.trim ());
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isEmpty (String text) {
        return text == null || text.trim ().isEmpty ();
    }
}