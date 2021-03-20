package org.tensorflow;

import java.nio.charset.Charset;
import org.tensorflow.Graph;

public final class OperationBuilder {
    private Graph graph;
    private long unsafeNativeHandle;

    private static native void addControlInput(long j, long j2);

    private static native void addInput(long j, long j2, int i);

    private static native void addInputList(long j, long[] jArr, int[] iArr);

    private static native long allocate(long j, String str, String str2);

    private static native long finish(long j);

    private static native void setAttrBool(long j, String str, boolean z);

    private static native void setAttrBoolList(long j, String str, boolean[] zArr);

    private static native void setAttrFloat(long j, String str, float f);

    private static native void setAttrFloatList(long j, String str, float[] fArr);

    private static native void setAttrInt(long j, String str, long j2);

    private static native void setAttrIntList(long j, String str, long[] jArr);

    private static native void setAttrShape(long j, String str, long[] jArr, int i);

    private static native void setAttrShapeList(long j, String str, long[] jArr, int[] iArr);

    private static native void setAttrString(long j, String str, byte[] bArr);

    private static native void setAttrStringList(long j, String str, Object[] objArr);

    private static native void setAttrTensor(long j, String str, long j2);

    private static native void setAttrTensorList(long j, String str, long[] jArr);

    private static native void setAttrType(long j, String str, int i);

    private static native void setAttrTypeList(long j, String str, int[] iArr);

    private static native void setDevice(long j, String str);

    OperationBuilder(Graph graph2, String type, String name) {
        this.graph = graph2;
        Graph.Reference r = graph2.ref();
        try {
            this.unsafeNativeHandle = allocate(r.nativeHandle(), type, name);
        } finally {
            r.close();
        }
    }

    public Operation build() {
        Graph.Reference r = this.graph.ref();
        try {
            Operation op = new Operation(this.graph, finish(this.unsafeNativeHandle));
            this.unsafeNativeHandle = 0;
            return op;
        } finally {
            r.close();
        }
    }

    public OperationBuilder addInput(Output<?> input) {
        Graph.Reference r = this.graph.ref();
        try {
            addInput(this.unsafeNativeHandle, input.mo6082op().getUnsafeNativeHandle(), input.index());
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder addControlInput(Operation control) {
        Graph.Reference r = this.graph.ref();
        try {
            addControlInput(this.unsafeNativeHandle, control.getUnsafeNativeHandle());
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder addInputList(Output<?>[] inputs) {
        Graph.Reference r = this.graph.ref();
        try {
            long[] opHandles = new long[inputs.length];
            int[] indices = new int[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                opHandles[i] = inputs[i].mo6082op().getUnsafeNativeHandle();
                indices[i] = inputs[i].index();
            }
            addInputList(this.unsafeNativeHandle, opHandles, indices);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setDevice(String device) {
        Graph.Reference r = this.graph.ref();
        try {
            setDevice(this.unsafeNativeHandle, device);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, String value) {
        setAttr(name, value.getBytes(Charset.forName("UTF-8")));
        return this;
    }

    public OperationBuilder setAttr(String name, byte[] value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrString(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, long value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrInt(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, long[] value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrIntList(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, float value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrFloat(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, float[] value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrFloatList(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, boolean value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrBool(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, boolean[] value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrBoolList(this.unsafeNativeHandle, name, value);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, DataType value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrType(this.unsafeNativeHandle, name, value.mo6030c());
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, DataType[] value) {
        int[] ctypes = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            ctypes[i] = value[i].mo6030c();
        }
        Graph.Reference r = this.graph.ref();
        try {
            setAttrTypeList(this.unsafeNativeHandle, name, ctypes);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, Tensor<?> value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrTensor(this.unsafeNativeHandle, name, value.getNativeHandle());
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, Tensor<?>[] value) {
        long[] handles = new long[value.length];
        int length = value.length;
        int i = 0;
        int idx = 0;
        while (i < length) {
            handles[idx] = value[i].getNativeHandle();
            i++;
            idx++;
        }
        Graph.Reference r = this.graph.ref();
        try {
            setAttrTensorList(this.unsafeNativeHandle, name, handles);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, Shape value) {
        Graph.Reference r = this.graph.ref();
        try {
            setAttrShape(this.unsafeNativeHandle, name, value.asArray(), value.numDimensions());
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, Shape[] value) {
        int shapeIdx;
        int[] numDimensions = new int[value.length];
        int totalNumDimensions = 0;
        for (int idx = 0; idx < value.length; idx++) {
            int n = value[idx].numDimensions();
            numDimensions[idx] = n;
            if (n > 0) {
                totalNumDimensions += n;
            }
        }
        long[] shapes = new long[totalNumDimensions];
        int shapeIdx2 = 0;
        for (Shape shape : value) {
            if (shape.numDimensions() > 0) {
                long[] asArray = shape.asArray();
                int length = asArray.length;
                int i = 0;
                while (true) {
                    shapeIdx = shapeIdx2;
                    if (i >= length) {
                        break;
                    }
                    shapeIdx2 = shapeIdx + 1;
                    shapes[shapeIdx] = asArray[i];
                    i++;
                }
                shapeIdx2 = shapeIdx;
            }
        }
        Graph.Reference r = this.graph.ref();
        try {
            setAttrShapeList(this.unsafeNativeHandle, name, shapes, numDimensions);
            return this;
        } finally {
            r.close();
        }
    }

    public OperationBuilder setAttr(String name, String[] value) {
        Charset utf8 = Charset.forName("UTF-8");
        Object[] objects = new Object[value.length];
        for (int i = 0; i < value.length; i++) {
            objects[i] = value[i].getBytes(utf8);
        }
        Graph.Reference r = this.graph.ref();
        try {
            setAttrStringList(this.unsafeNativeHandle, name, objects);
            return this;
        } finally {
            r.close();
        }
    }
}
