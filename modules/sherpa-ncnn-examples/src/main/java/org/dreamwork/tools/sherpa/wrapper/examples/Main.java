package org.dreamwork.tools.sherpa.wrapper.examples;

import com.google.gson.Gson;
import org.dreamwork.app.bootloader.IBootable;
import org.dreamwork.cli.Argument;
import org.dreamwork.cli.ArgumentParser;
import org.dreamwork.config.IConfiguration;
import org.dreamwork.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@IBootable (argumentDef = "sherpa-ncnn-examples.json")
public class Main {
    public static void main (String[] args) throws Exception {
        System.out.println (System.getProperty ("os.name"));
//        ApplicationBootloader.run (Main.class, args);
    }

    public void start (IConfiguration conf) {
        System.out.println (conf);
    }

    private static void runDirect (String... args) throws IOException {
        ArgumentParser parser = null;
        List<Argument> json = null;
        ClassLoader loader = Thread.currentThread ().getContextClassLoader ();
        try (InputStream in = loader.getResourceAsStream ("sherpa-ncnn-examples.json")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader (new InputStreamReader (in, StandardCharsets.UTF_8));
                json = new Gson ().fromJson (reader, Argument.AS_LIST);
                parser = new ArgumentParser (json);
                parser.parse (args);
            }
        }

        if (parser == null) {
            System.err.println ("cannot parse cli arguments");
            System.exit (-1);
        }

        if (parser.isArgPresent ('h')) {
            parser.showHelp ();
            System.exit (0);
        }

        boolean ok = true;
        for (Argument arg : json) {
            if (arg.required && !parser.isArgPresent (arg.longOption)) {
                String shortOption = arg.shortOption, longOption = arg.longOption;
                if (StringUtil.isEmpty (shortOption)) {
                    shortOption = "";
                    longOption = "--" + longOption;
                } else {
                    shortOption = "-" + shortOption;
                    longOption = " --" + longOption;
                }
                System.err.printf (
                        "Argument %s%s is required, but it is not present!%n",
                        shortOption, longOption
                );
                ok = false;
            }
        }
/*
        if (!ok) {
            System.exit (-1);
        }
*/

//        System.out.println (SherpaNcnn.class);
        try {
            Class.forName ("com.k2fsa.sherpa.ncnn.SherpaNcnn");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException (e);
        }
    }
}