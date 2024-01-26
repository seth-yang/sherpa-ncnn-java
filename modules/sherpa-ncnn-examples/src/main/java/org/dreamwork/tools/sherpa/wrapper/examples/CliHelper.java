package org.dreamwork.tools.sherpa.wrapper.examples;

import com.google.gson.Gson;
import org.dreamwork.cli.Argument;
import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.tools.sherpa.wrapper.SherpaConfig;
import org.dreamwork.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CliHelper {
    private static final ClassLoader loader = CliHelper.class.getClassLoader ();
    private static final Gson g = new Gson ();

    public static ArgumentParser getArgumentsParser (String... definitions) throws IOException {
        Map<String, Argument> map = new HashMap<> ();
        loadArgumentDefinition ("sherpa-ncnn-examples", map);
        for (String definition : definitions) {
            loadArgumentDefinition (definition, map);
        }
        if (!map.values ().isEmpty ()) {
            List<Argument> args = new ArrayList<> (map.values ());
            return new ArgumentParser (args);
        }

        return null;
    }

    public static boolean isRequiredArgMissing (ArgumentParser parser) {
        Collection<Argument> all = parser.getAllArguments ();
        boolean missing = false;
        for (Argument arg : all) {
            if (arg.required && !parser.isArgPresent (arg.longOption)) {
                System.err.printf ("Error: option --%s is required, but it is not present.%n", arg.longOption);
                missing = true;
            }
        }

        return missing;
    }

    public static SherpaConfig convert (ArgumentParser parser) {
        SherpaConfig conf = new SherpaConfig ();
        if (parser.isArgPresent ('d')) {
            conf.basedir = parser.getValue ('d');
        }
        if (parser.isArgPresent ('D')) {
            conf.useGpu = false;
        }
        if (parser.isArgPresent ('n')) {
            conf.modelName = parser.getValue ('n');
        }
        if (parser.isArgPresent ("threads")) {
            conf.threads = Integer.parseInt (parser.getValue ("threads"));
        }
        if (parser.isArgPresent ("encoder-param")) {
            conf.encoderParam = parser.getValue ("encoder-param");
        }
        if (parser.isArgPresent ("encoder-bin")) {
            conf.encoderBin = parser.getValue ("encoder-bin");
        }
        if (parser.isArgPresent ("decoder-param")) {
            conf.decoderParam = parser.getValue ("decoder-param");
        }
        if (parser.isArgPresent ("decoder-bin")) {
            conf.decoderBin = parser.getValue ("decoder-bin");
        }
        if (parser.isArgPresent ("joiner-param")) {
            conf.joinerParam = parser.getValue ("joiner-param");
        }
        if (parser.isArgPresent ("joiner-bin")) {
            conf.joinerBin = parser.getValue ("joiner-bin");
        }
        if (parser.isArgPresent ('t')) {
            conf.tokens = parser.getValue ('t');
        }
        if (parser.isArgPresent ('r')) {
            conf.sampleRate = Float.parseFloat (parser.getValue ('r'));
        }
        if (parser.isArgPresent ("dim")) {
            conf.featureDim = Integer.parseInt (parser.getValue ("dim"));
        }
        if (parser.isArgPresent ('m')) {
            conf.decoderMethod = parser.getValue ('m');
        }
        if (parser.isArgPresent ('p')) {
            conf.activePaths = Integer.parseInt (parser.getValue ('p'));
        }
        if (parser.isArgPresent ("disable-endpoint")) {
            conf.endpointEnabled = false;
        }
        if (parser.isArgPresent ("silence-r1")) {
            conf.r1Silence = Float.parseFloat (parser.getValue ("silence-r1"));
        }
        if (parser.isArgPresent ("silence-r2")) {
            conf.r2Silence = Float.parseFloat (parser.getValue ("silence-r2"));
        }
        if (parser.isArgPresent ("utterance-r3")) {
            conf.r3utterance = Float.parseFloat (parser.getValue ("utterance-r3"));
        }
        if (parser.isArgPresent ("hot-words-file")) {
            conf.hotWordsFile = parser.getValue ("hot-words-file");
        }
        if (parser.isArgPresent ("hot-words-score")) {
            conf.hotWordScore = Float.parseFloat (parser.getValue ("hot-words-score"));
        }

        return conf;
    }

    private static void loadArgumentDefinition (String name, Map<String, Argument> map) throws IOException {
        if (!name.endsWith (".json")) {
            name += ".json";
        }
        try (InputStream in = loader.getResourceAsStream (name)) {
            if (in != null) {
                BufferedReader reader = new BufferedReader (new InputStreamReader (in, StandardCharsets.UTF_8));
                List<Argument> list = g.fromJson (reader, Argument.AS_LIST);
                if (list != null && !list.isEmpty ()) {
                    list.forEach (item -> {
                        String key = item.shortOption;
                        if (StringUtil.isEmpty (key)) {
                            key = item.longOption;
                        }
                        if (!StringUtil.isEmpty (key)) {
                            map.put (key, item);
                        }
                    });
                }
            }
        }
    }
}
