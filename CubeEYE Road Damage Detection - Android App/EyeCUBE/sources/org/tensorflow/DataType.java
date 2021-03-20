package org.tensorflow;

import java.util.HashMap;
import java.util.Map;
import org.tensorflow.types.UInt8;

public enum DataType {
    FLOAT(1),
    DOUBLE(2),
    INT32(3),
    UINT8(4),
    STRING(7),
    INT64(9),
    BOOL(10);
    
    private static final Map<Class<?>, DataType> typeCodes = null;
    private static final DataType[] values = null;
    private final int value;

    static {
        values = values();
        typeCodes = new HashMap();
        typeCodes.put(Float.class, FLOAT);
        typeCodes.put(Double.class, DOUBLE);
        typeCodes.put(Integer.class, INT32);
        typeCodes.put(UInt8.class, UINT8);
        typeCodes.put(Long.class, INT64);
        typeCodes.put(Boolean.class, BOOL);
        typeCodes.put(String.class, STRING);
    }

    private DataType(int value2) {
        this.value = value2;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: c */
    public int mo6030c() {
        return this.value;
    }

    static DataType fromC(int c) {
        for (DataType t : values) {
            if (t.value == c) {
                return t;
            }
        }
        throw new IllegalArgumentException("DataType " + c + " is not recognized in Java (version " + TensorFlow.version() + ")");
    }

    public static DataType fromClass(Class<?> c) {
        DataType dtype = typeCodes.get(c);
        if (dtype != null) {
            return dtype;
        }
        throw new IllegalArgumentException(c.getName() + " objects cannot be used as elements in a TensorFlow Tensor");
    }
}
