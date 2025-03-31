# sherpa-ncnn-jni
The [sherpa-ncnn](https://github.com/k2-fsa/sherpa-ncnn) project provides APIs for many programming languages, even including Kotlin APIs, but does not provide a direct Java API.
This project is powered by sherpa-ncnn's [Android sample code](https://github.com/k2-fsa/sherpa-ncnn/blob/master/android/SherpaNcnn/app/src/main/java/com/k2fsa/sherpa/ncnn/?_blank).
Adapted and fine-tuned the API to be more consistent with Java's idiomatic usage.

In addition to fine-tuning the original sherpa-ncnn API; in order to facilitate the calling of Java client programs, the parameters of sherpa-ncnn have been re-encapsulated;
In addition, an auxiliary class that simply encapsulates the Java Audio API is provided.

For usage, please see [Example Code](../sherpa-ncnn-examples/)