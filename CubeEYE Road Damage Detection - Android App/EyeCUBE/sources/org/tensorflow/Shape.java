package org.tensorflow;

import java.util.Arrays;

public final class Shape {
    private long[] shape;

    public static Shape unknown() {
        return new Shape((long[]) null);
    }

    public static Shape scalar() {
        return new Shape(new long[0]);
    }

    public static Shape make(long firstDimensionSize, long... otherDimensionSizes) {
        long[] shape2 = new long[(otherDimensionSizes.length + 1)];
        shape2[0] = firstDimensionSize;
        System.arraycopy(otherDimensionSizes, 0, shape2, 1, otherDimensionSizes.length);
        return new Shape(shape2);
    }

    public int numDimensions() {
        if (this.shape == null) {
            return -1;
        }
        return this.shape.length;
    }

    public long size(int i) {
        return this.shape[i];
    }

    public int hashCode() {
        return Arrays.hashCode(this.shape);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Shape) || !Arrays.equals(this.shape, ((Shape) obj).shape)) {
            return super.equals(obj);
        }
        return !hasUnknownDimension();
    }

    public String toString() {
        if (this.shape == null) {
            return "<unknown>";
        }
        return Arrays.toString(this.shape).replace("-1", "?");
    }

    Shape(long[] shape2) {
        this.shape = shape2;
    }

    /* access modifiers changed from: package-private */
    public long[] asArray() {
        return this.shape;
    }

    private boolean hasUnknownDimension() {
        if (this.shape == null) {
            return true;
        }
        for (long dimension : this.shape) {
            if (dimension == -1) {
                return true;
            }
        }
        return false;
    }
}
