package org.dreamwork.tools.sherpa.wrapper;

public enum ModelType {
    Default,    // 0
    Transducer, // 1
    BilingualZhEn,  // 2
    StreamingZipFormerEn,   // 3
    StreamingZipFormerFr,   // 4
    StreamingZipFormerBilingualZhEn,    // 5
    StreamingZipFormerSmallBilingualZhEn,   // 6

    ;

    public static ModelType of (String name) {
        for (ModelType value : values ()) {
            if (value.name ().equalsIgnoreCase (name)) {
                return value;
            }
        }
        return Default;
    }

    public static ModelType of (int index) {
        if (index <= 0 || index > values ().length) {
            return Default;
        }
        return values() [index];
    }
}
