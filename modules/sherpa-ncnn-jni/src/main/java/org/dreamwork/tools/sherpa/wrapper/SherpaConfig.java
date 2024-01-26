package org.dreamwork.tools.sherpa.wrapper;

public class SherpaConfig {
    // # the root directory of sherpa-ncnn models
    //# default to ../models
    //#sherpa.ncnn.model.dir                       = ../models
    public  String basedir = "../models";

    //# Model name predefined by sherpa-ncnn-java
    //# default to 'default'
    //#sherpa.ncnn.model.name                      =
    public  String modelName;
    
    //# enable using gpu
    //# default to true
    //#sherpa.ncnn.model.gpu.enabled               = true
    public  boolean useGpu = true;
    
    //# The number of cpu threads used by the sherpa-ncnn model.
    //# set to -1 to let sherpa-ncnn-java decide it.
    //# default to -1
    //#sherpa.ncnn.model.threads                   = -1
    public  int threads = -1;
    
    //# Path to sherpa-ncnn encoder's param file.
    //#sherpa.ncnn.model.encoder.param             =
    public  String encoderParam;
    
    //# Path to sherpa-ncnn encoder's bin file.
    //#sherpa.ncnn.model.encoder.bin               =
    public  String encoderBin;
    
    //# Path to sherpa-ncnn decoder's param file.
    //#sherpa.ncnn.model.decoder.param             =
    public  String decoderParam;
    
    //# Path to sherpa-ncnn decoder's bin file.
    //#sherpa.ncnn.model.decoder.bin               =
    public  String decoderBin;
    
    //# Path to sherpa-ncnn joiner's param file.
    //#sherpa.ncnn.model.joiner.param              =
    public  String joinerParam;
    
    //# Path to sherpa-ncnn joiner's bin file.
    //#sherpa.ncnn.model.joiner.bin                =
    public  String joinerBin;
    
    //# Path to sherpa-ncnn model tokens file.
    //#sherpa.ncnn.model.tokens                    =
    public  String tokens; 
    
    //# Audio sample rate. default to 6000 (16K Hz)
    //#sherpa.ncnn.feature.sample.rate             = 16000
    public float sampleRate = 16000f;
    
    //# ??
    //#sherpa.ncnn.feature.dim                     = 80
    public  int featureDim = 80;
    
    //# sherpa-ncnn audio decoding method. default to greedy_search
    //#sherpa.ncnn.decoder.method                  = greedy_search
    public  String decoderMethod = "greedy_search";
    
    //# Number of active path for sherpa-ncnn audio decoding. default to 4
    //#sherpa.ncnn.decoder.active.paths            = 4
    public  int activePaths = 4;
    
    //# enable auto endpoint-detect or not. default to true
    //#sherpa.ncnn.asr.endpoint.enabled            = true
    public  boolean endpointEnabled = true;
    
    //#
    //#sherpa.ncnn.asr.rule1.min.trailing.silence  = 2.4
    //#sherpa.ncnn.asr.rule2.min.trailing.silence  = 1.4
    //#sherpa.ncnn.asr.rule3.min.utterance.length  = 20
    public  float r1Silence = 2.4f;
    public  float r2Silence = 1.4f;
    public  float r3utterance = 20f;
    
    //# Path to hot-words file
    //#sherpa.ncnn.asr.hot-words.file              =
    public  String hotWordsFile = "";
    
    //#
    //#sherpa.ncnn.asr.hot-words.score             = 1.5
    public float hotWordScore = 1.5f;
}
