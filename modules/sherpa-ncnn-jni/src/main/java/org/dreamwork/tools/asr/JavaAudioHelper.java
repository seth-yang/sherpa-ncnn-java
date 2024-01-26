package org.dreamwork.tools.asr;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class JavaAudioHelper {
    public static AudioFormat createAudioFormat () {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED; // the pcm format
        float rate = 16000.0f; // using 16 kHz
        int channels = 1; // single channel
        int sampleSize = 16; // sampleSize 16bit
        boolean isBigEndian = false; // using little endian

        return new AudioFormat (
                encoding, rate, sampleSize, channels,
                (sampleSize / 8) * channels, rate, isBigEndian
        );
    }

    public static TargetDataLine initMic (AudioFormat format) {
        DataLine.Info info = new DataLine.Info (TargetDataLine.class, format);

        // check system support such data format
        if (!AudioSystem.isLineSupported (info)) {
            System.err.println (info + " not supported.");
            return null;
        }

        // open a line for capture.
        TargetDataLine line;
        try {
            line = (TargetDataLine) AudioSystem.getLine (info);
            line.open (format, line.getBufferSize ());
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
        return line;
    }
}
