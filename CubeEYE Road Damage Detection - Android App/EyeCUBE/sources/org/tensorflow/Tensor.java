package org.tensorflow;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.HashMap;

public final class Tensor<T> implements AutoCloseable {
    private static HashMap<Class<?>, DataType> classDataTypes = new HashMap<>();
    private DataType dtype;
    private long nativeHandle;
    private long[] shapeCopy = null;

    private static native long allocate(int i, long[] jArr, long j);

    private static native long allocateNonScalarBytes(long[] jArr, Object[] objArr);

    private static native long allocateScalarBytes(byte[] bArr);

    private static native ByteBuffer buffer(long j);

    private static native void delete(long j);

    private static native int dtype(long j);

    private static native void readNDArray(long j, Object obj);

    private static native boolean scalarBoolean(long j);

    private static native byte[] scalarBytes(long j);

    private static native double scalarDouble(long j);

    private static native float scalarFloat(long j);

    private static native int scalarInt(long j);

    private static native long scalarLong(long j);

    private static native void setValue(long j, Object obj);

    private static native long[] shape(long j);

    public static <T> Tensor<T> create(Object obj, Class<T> type) {
        DataType dtype2 = DataType.fromClass(type);
        if (objectCompatWithType(obj, dtype2)) {
            return create(obj, dtype2);
        }
        throw new IllegalArgumentException("DataType of object does not match T (expected " + dtype2 + ", got " + dataTypeOf(obj) + ")");
    }

    public static Tensor<?> create(Object obj) {
        return create(obj, dataTypeOf(obj));
    }

    private static Tensor<?> create(Object obj, DataType dtype2) {
        Tensor<?> t = new Tensor<>(dtype2);
        t.shapeCopy = new long[numDimensions(obj, dtype2)];
        fillShape(obj, 0, t.shapeCopy);
        if (t.dtype != DataType.STRING) {
            t.nativeHandle = allocate(t.dtype.mo6030c(), t.shapeCopy, (long) (elemByteSize(t.dtype) * numElements(t.shapeCopy)));
            setValue(t.nativeHandle, obj);
        } else if (t.shapeCopy.length != 0) {
            t.nativeHandle = allocateNonScalarBytes(t.shapeCopy, (Object[]) obj);
        } else {
            t.nativeHandle = allocateScalarBytes((byte[]) obj);
        }
        return t;
    }

    public static Tensor<Integer> create(long[] shape, IntBuffer data) {
        Tensor<Integer> t = allocateForBuffer(DataType.INT32, shape, data.remaining());
        t.buffer().asIntBuffer().put(data);
        return t;
    }

    public static Tensor<Float> create(long[] shape, FloatBuffer data) {
        Tensor<Float> t = allocateForBuffer(DataType.FLOAT, shape, data.remaining());
        t.buffer().asFloatBuffer().put(data);
        return t;
    }

    public static Tensor<Double> create(long[] shape, DoubleBuffer data) {
        Tensor<Double> t = allocateForBuffer(DataType.DOUBLE, shape, data.remaining());
        t.buffer().asDoubleBuffer().put(data);
        return t;
    }

    public static Tensor<Long> create(long[] shape, LongBuffer data) {
        Tensor<Long> t = allocateForBuffer(DataType.INT64, shape, data.remaining());
        t.buffer().asLongBuffer().put(data);
        return t;
    }

    public static <T> Tensor<T> create(Class<T> type, long[] shape, ByteBuffer data) {
        return create(DataType.fromClass(type), shape, data);
    }

    private static Tensor<?> create(DataType dtype2, long[] shape, ByteBuffer data) {
        int nremaining;
        if (dtype2 != DataType.STRING) {
            int elemBytes = elemByteSize(dtype2);
            if (data.remaining() % elemBytes != 0) {
                throw new IllegalArgumentException(String.format("ByteBuffer with %d bytes is not compatible with a %s Tensor (%d bytes/element)", new Object[]{Integer.valueOf(data.remaining()), dtype2.toString(), Integer.valueOf(elemBytes)}));
            }
            nremaining = data.remaining() / elemBytes;
        } else {
            nremaining = data.remaining();
        }
        Tensor<?> t = allocateForBuffer(dtype2, shape, nremaining);
        t.buffer().put(data);
        return t;
    }

    public <U> Tensor<U> expect(Class<U> type) {
        DataType dt = DataType.fromClass(type);
        if (dt.equals(this.dtype)) {
            return this;
        }
        throw new IllegalArgumentException("Cannot cast from tensor of " + this.dtype + " to tensor of " + dt);
    }

    private static <T> Tensor<T> allocateForBuffer(DataType dataType, long[] shape, int nBuffered) {
        int nbytes;
        int nflattened = numElements(shape);
        if (dataType == DataType.STRING) {
            nbytes = nBuffered;
        } else if (nBuffered != nflattened) {
            throw incompatibleBuffer(nBuffered, shape);
        } else {
            nbytes = nflattened * elemByteSize(dataType);
        }
        Tensor<T> t = new Tensor<>(dataType);
        t.shapeCopy = Arrays.copyOf(shape, shape.length);
        t.nativeHandle = allocate(t.dtype.mo6030c(), t.shapeCopy, (long) nbytes);
        return t;
    }

    public void close() {
        if (this.nativeHandle != 0) {
            delete(this.nativeHandle);
            this.nativeHandle = 0;
        }
    }

    public DataType dataType() {
        return this.dtype;
    }

    public int numDimensions() {
        return this.shapeCopy.length;
    }

    public int numBytes() {
        return buffer().remaining();
    }

    public int numElements() {
        return numElements(this.shapeCopy);
    }

    public long[] shape() {
        return this.shapeCopy;
    }

    public float floatValue() {
        return scalarFloat(this.nativeHandle);
    }

    public double doubleValue() {
        return scalarDouble(this.nativeHandle);
    }

    public int intValue() {
        return scalarInt(this.nativeHandle);
    }

    public long longValue() {
        return scalarLong(this.nativeHandle);
    }

    public boolean booleanValue() {
        return scalarBoolean(this.nativeHandle);
    }

    public byte[] bytesValue() {
        return scalarBytes(this.nativeHandle);
    }

    public <U> U copyTo(U dst) {
        throwExceptionIfTypeIsIncompatible(dst);
        readNDArray(this.nativeHandle, dst);
        return dst;
    }

    public void writeTo(IntBuffer dst) {
        if (this.dtype != DataType.INT32) {
            throw incompatibleBuffer((Buffer) dst, this.dtype);
        }
        dst.put(buffer().asIntBuffer());
    }

    public void writeTo(FloatBuffer dst) {
        if (this.dtype != DataType.FLOAT) {
            throw incompatibleBuffer((Buffer) dst, this.dtype);
        }
        dst.put(buffer().asFloatBuffer());
    }

    public void writeTo(DoubleBuffer dst) {
        if (this.dtype != DataType.DOUBLE) {
            throw incompatibleBuffer((Buffer) dst, this.dtype);
        }
        dst.put(buffer().asDoubleBuffer());
    }

    public void writeTo(LongBuffer dst) {
        if (this.dtype != DataType.INT64) {
            throw incompatibleBuffer((Buffer) dst, this.dtype);
        }
        dst.put(buffer().asLongBuffer());
    }

    public void writeTo(ByteBuffer dst) {
        dst.put(buffer());
    }

    public String toString() {
        return String.format("%s tensor with shape %s", new Object[]{this.dtype.toString(), Arrays.toString(shape())});
    }

    static Tensor<?> fromHandle(long handle) {
        Tensor<?> t = new Tensor<>(DataType.fromC(dtype(handle)));
        t.shapeCopy = shape(handle);
        t.nativeHandle = handle;
        return t;
    }

    /* access modifiers changed from: package-private */
    public long getNativeHandle() {
        return this.nativeHandle;
    }

    private Tensor(DataType t) {
        this.dtype = t;
    }

    private ByteBuffer buffer() {
        return buffer(this.nativeHandle).order(ByteOrder.nativeOrder());
    }

    private static IllegalArgumentException incompatibleBuffer(Buffer buf, DataType dataType) {
        return new IllegalArgumentException(String.format("cannot use %s with Tensor of type %s", new Object[]{buf.getClass().getName(), dataType}));
    }

    private static IllegalArgumentException incompatibleBuffer(int numElements, long[] shape) {
        return new IllegalArgumentException(String.format("buffer with %d elements is not compatible with a Tensor with shape %s", new Object[]{Integer.valueOf(numElements), Arrays.toString(shape)}));
    }

    private static int numElements(long[] shape) {
        int n = 1;
        for (long j : shape) {
            n *= (int) j;
        }
        return n;
    }

    private static int elemByteSize(DataType dataType) {
        switch (dataType) {
            case FLOAT:
            case INT32:
                return 4;
            case DOUBLE:
            case INT64:
                return 8;
            case BOOL:
            case UINT8:
                return 1;
            case STRING:
                throw new IllegalArgumentException("STRING tensors do not have a fixed element size");
            default:
                throw new IllegalArgumentException("DataType " + dataType + " is not supported yet");
        }
    }

    private static void throwExceptionIfNotByteOfByteArrays(Object array) {
        if (!array.getClass().getName().equals("[[B")) {
            throw new IllegalArgumentException("object cannot be converted to a Tensor as it includes an array with null elements");
        }
    }

    static {
        classDataTypes.put(Integer.TYPE, DataType.INT32);
        classDataTypes.put(Integer.class, DataType.INT32);
        classDataTypes.put(Long.TYPE, DataType.INT64);
        classDataTypes.put(Long.class, DataType.INT64);
        classDataTypes.put(Float.TYPE, DataType.FLOAT);
        classDataTypes.put(Float.class, DataType.FLOAT);
        classDataTypes.put(Double.TYPE, DataType.DOUBLE);
        classDataTypes.put(Double.class, DataType.DOUBLE);
        classDataTypes.put(Byte.TYPE, DataType.STRING);
        classDataTypes.put(Byte.class, DataType.STRING);
        classDataTypes.put(Boolean.TYPE, DataType.BOOL);
        classDataTypes.put(Boolean.class, DataType.BOOL);
        TensorFlow.init();
    }

    private static Class<?> baseObjType(Object o) {
        Class<?> c = o.getClass();
        while (c.isArray()) {
            c = c.getComponentType();
        }
        return c;
    }

    private static DataType dataTypeOf(Object o) {
        return dataTypeFromClass(baseObjType(o));
    }

    private static DataType dataTypeFromClass(Class<?> c) {
        DataType ret = classDataTypes.get(c);
        if (ret != null) {
            return ret;
        }
        throw new IllegalArgumentException("cannot create Tensors of type " + c.getName());
    }

    private static int numDimensions(Object o, DataType dtype2) {
        int ret = numArrayDimensions(o);
        if (dtype2 != DataType.STRING || ret <= 0) {
            return ret;
        }
        return ret - 1;
    }

    private static int numArrayDimensions(Object o) {
        Class<?> c = o.getClass();
        int i = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            i++;
        }
        return i;
    }

    private static void fillShape(Object o, int dim, long[] shape) {
        if (shape != null && dim != shape.length) {
            int len = Array.getLength(o);
            if (len == 0) {
                throw new IllegalArgumentException("cannot create Tensors with a 0 dimension");
            }
            if (shape[dim] == 0) {
                shape[dim] = (long) len;
            } else if (shape[dim] != ((long) len)) {
                throw new IllegalArgumentException(String.format("mismatched lengths (%d and %d) in dimension %d", new Object[]{Long.valueOf(shape[dim]), Integer.valueOf(len), Integer.valueOf(dim)}));
            }
            for (int i = 0; i < len; i++) {
                fillShape(Array.get(o, i), dim + 1, shape);
            }
        }
    }

    private static boolean objectCompatWithType(Object obj, DataType dtype2) {
        Class<?> c = baseObjType(obj);
        DataType dto = dataTypeFromClass(c);
        int nd = numDimensions(obj, dto);
        if (!c.isPrimitive() && c != String.class && nd != 0) {
            throw new IllegalArgumentException("cannot create non-scalar Tensors from arrays of boxed values");
        } else if (dto.equals(dtype2)) {
            return true;
        } else {
            if (dto == DataType.STRING && dtype2 == DataType.UINT8) {
                return true;
            }
            return false;
        }
    }

    private void throwExceptionIfTypeIsIncompatible(Object o) {
        int rank = numDimensions();
        int oRank = numDimensions(o, this.dtype);
        if (oRank != rank) {
            throw new IllegalArgumentException(String.format("cannot copy Tensor with %d dimensions into an object with %d", new Object[]{Integer.valueOf(rank), Integer.valueOf(oRank)}));
        } else if (!objectCompatWithType(o, this.dtype)) {
            throw new IllegalArgumentException(String.format("cannot copy Tensor with DataType %s into an object of type %s", new Object[]{this.dtype.toString(), o.getClass().getName()}));
        } else {
            long[] oShape = new long[rank];
            fillShape(o, 0, oShape);
            for (int i = 0; i < oShape.length; i++) {
                if (oShape[i] != shape()[i]) {
                    throw new IllegalArgumentException(String.format("cannot copy Tensor with shape %s into object with shape %s", new Object[]{Arrays.toString(shape()), Arrays.toString(oShape)}));
                }
            }
        }
    }
}
