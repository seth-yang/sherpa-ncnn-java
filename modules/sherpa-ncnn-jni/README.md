# sherpa-ncnn-jni
[sherpa-ncnn](https://github.com/k2-fsa/sherpa-ncnn) 项目提供了许多编程语言的 API，甚至包括了 Kotlin 的API，但未提供直接的 Java API。
这个项目由 sherpa-ncnn 的 [Android 示例代码](https://github.com/k2-fsa/sherpa-ncnn/blob/master/android/SherpaNcnn/app/src/main/java/com/k2fsa/sherpa/ncnn/?_blank)
改编而来，并为更加符合 Java 的习惯使用方法对 API 进行微调。

除了对原始的 sherpa-ncnn API 微调外；为了方便 Java 客户程序的调用，针对 sherpa-ncnn 的参数进行了二次封装；
另外还提供了一个简单封装 Java Audio API 的辅助类。 

使用方法请参见 [示例代码](../sherpa-ncnn-examples/)