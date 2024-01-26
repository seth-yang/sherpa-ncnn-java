package com.k2fsa.sherpa.ncnn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SherpaNcnn implements Closeable {
    private final RecognizerConfig config;

    private final long ptr;

    private transient boolean closed = false;

    public SherpaNcnn (RecognizerConfig config) {
        this.config = config;
        ptr = newFromFile (config);
    }

    public void acceptSamples (float[] samples) {
        acceptWaveform (ptr, samples, config.getFeatConfig ().getSampleRate ());
    }

    public boolean isReady () {
        return isReady (ptr);
    }

    public void decode () {
        decode (ptr);
    }

    public void inputFinished () {
        inputFinished (ptr);
    }

    public boolean isEndpoint () {
        return isEndpoint (ptr);
    }

    public void reset (boolean recreate) {
        reset (ptr, recreate);
    }

    public String getText () {
        return getText (ptr);
    }

    @Override
    public void close () {
        if (!closed) {
            delete (ptr);
            closed = true;
        }
    }

    ///////////////////////////// native functions ///////////////////////
    private native long newFromFile (RecognizerConfig config);

    private native void delete (long ptr);
    private native void acceptWaveform (long ptr, float[] samples, float sampleRate);
    private native void inputFinished(long ptr);
    private native boolean isReady(long ptr);
    private native void decode(long ptr);
    private native boolean isEndpoint(long ptr);
    private native void reset(long ptr, boolean recreate);
    private native String getText(long ptr);

    private static void expandNativeLibs () throws IOException {
        String root = System.getProperty ("user.home") + "/.sherpa-ncnn/native-libs";
        String os   = System.getProperty ("os.name").toLowerCase ();
        String arch = System.getProperty ("os.arch");
        if ("amd64".equals (arch)) {
            arch = "x86-64";
        }
        Path dir = Paths.get (root, os, arch);
        if (Files.notExists (dir)) {
            Files.createDirectories (dir);
            ClassLoader loader = SherpaNcnn.class.getClassLoader ();
            String prefix = "native-libs/" + os + '/' + arch ;
            try (InputStream in = loader.getResourceAsStream (prefix)) {
                if (in != null) {
                    BufferedReader reader = new BufferedReader (new InputStreamReader (in));
                    String fileName;
                    while ((fileName = reader.readLine ()) != null) {
                        String name = prefix + "/" + fileName;
                        Path target = Paths.get (root, os, arch, fileName);
                        try (InputStream source = loader.getResourceAsStream (name)) {
                            if (source != null) {
                                System.out.printf ("extracting %s into %s...%n", fileName, target);
                                Files.copy (source, target);
                            } else {
                                throw new IOException ("cannot load native library: " + fileName);
                            }
                        }
                    }
                } else {
                    throw new IOException (
                            String.format ("Cannot load native libraries for %s_%s", os, arch)
                    );
                }
            }
        }

        if (Files.exists (dir)) {
            try (Stream<Path> stream = Files.list (dir)) {
                stream.forEach (path -> {
                    System.out.printf ("loading native library: %s...%n", path);
                    System.load (path.toString ());
                });
            }
        }
    }

    static {
        try {
            expandNativeLibs ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }
}