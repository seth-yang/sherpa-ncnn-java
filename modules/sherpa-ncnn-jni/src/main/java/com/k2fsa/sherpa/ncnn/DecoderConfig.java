package com.k2fsa.sherpa.ncnn;

public class DecoderConfig {
    private String method = "modified_beam_search";
    private int numActivePaths = 4;

    public DecoderConfig () {
    }

    public DecoderConfig (String method, int numActivePaths) {
        this.method = method;
        this.numActivePaths = numActivePaths;
    }

    public String getMethod () {
        return method;
    }

    public void setMethod (String method) {
        this.method = method;
    }

    public int getNumActivePaths () {
        return numActivePaths;
    }

    public void setNumActivePaths (int numActivePaths) {
        this.numActivePaths = numActivePaths;
    }
}
