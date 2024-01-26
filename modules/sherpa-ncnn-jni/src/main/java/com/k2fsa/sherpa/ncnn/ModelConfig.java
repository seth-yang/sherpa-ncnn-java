package com.k2fsa.sherpa.ncnn;

public class ModelConfig {
    private String encoderParam, encoderBin, decoderParam, decoderBin, joinerParam,
            joinerBin, tokens;
    private int numThreads = 1;
    private boolean useGPU = true;

    public ModelConfig () {
    }

    public ModelConfig (String encoderParam, String encoderBin, String decoderParam, String decoderBin,
                        String joinerParam, String joinerBin, String tokens, int numThreads, boolean useGPU) {
        this.encoderParam = encoderParam;
        this.encoderBin = encoderBin;
        this.decoderParam = decoderParam;
        this.decoderBin = decoderBin;
        this.joinerParam = joinerParam;
        this.joinerBin = joinerBin;
        this.tokens = tokens;
        this.numThreads = numThreads;
        this.useGPU = useGPU;
    }

    public String getEncoderParam () {
        return encoderParam;
    }

    public void setEncoderParam (String encoderParam) {
        this.encoderParam = encoderParam;
    }

    public String getEncoderBin () {
        return encoderBin;
    }

    public void setEncoderBin (String encoderBin) {
        this.encoderBin = encoderBin;
    }

    public String getDecoderParam () {
        return decoderParam;
    }

    public void setDecoderParam (String decoderParam) {
        this.decoderParam = decoderParam;
    }

    public String getDecoderBin () {
        return decoderBin;
    }

    public void setDecoderBin (String decoderBin) {
        this.decoderBin = decoderBin;
    }

    public String getJoinerParam () {
        return joinerParam;
    }

    public void setJoinerParam (String joinerParam) {
        this.joinerParam = joinerParam;
    }

    public String getJoinerBin () {
        return joinerBin;
    }

    public void setJoinerBin (String joinerBin) {
        this.joinerBin = joinerBin;
    }

    public String getTokens () {
        return tokens;
    }

    public void setTokens (String tokens) {
        this.tokens = tokens;
    }

    public int getNumThreads () {
        return numThreads;
    }

    public void setNumThreads (int numThreads) {
        this.numThreads = numThreads;
    }

    public boolean isUseGPU () {
        return useGPU;
    }

    public void setUseGPU (boolean useGPU) {
        this.useGPU = useGPU;
    }
}