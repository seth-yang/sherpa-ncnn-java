package com.k2fsa.sherpa.ncnn;

public class FeatureExtractorConfig {
    private float sampleRate;
    private int featureDim;

    public FeatureExtractorConfig () {
    }

    public FeatureExtractorConfig (float sampleRate, int featureDim) {
        this.sampleRate = sampleRate;
        this.featureDim = featureDim;
    }

    public float getSampleRate () {
        return sampleRate;
    }

    public void setSampleRate (float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getFeatureDim () {
        return featureDim;
    }

    public void setFeatureDim (int featureDim) {
        this.featureDim = featureDim;
    }
}
