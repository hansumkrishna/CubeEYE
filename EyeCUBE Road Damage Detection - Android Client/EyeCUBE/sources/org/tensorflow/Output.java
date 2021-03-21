package org.tensorflow;

import java.util.Objects;

public final class Output<T> implements Operand<T> {
    private final int index;
    private final Operation operation;

    public Output(Operation op, int idx) {
        this.operation = op;
        this.index = idx;
    }

    /* renamed from: op */
    public Operation mo6082op() {
        return this.operation;
    }

    public int index() {
        return this.index;
    }

    public Shape shape() {
        return new Shape(this.operation.shape(this.index));
    }

    public DataType dataType() {
        return this.operation.dtype(this.index);
    }

    public Output<T> asOutput() {
        return this;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.operation, Integer.valueOf(this.index)});
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Output)) {
            return false;
        }
        Output<?> that = (Output) o;
        if (this.index != that.index || !this.operation.equals(that.operation)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("<%s '%s:%d' shape=%s dtype=%s>", new Object[]{this.operation.type(), this.operation.name(), Integer.valueOf(this.index), shape().toString(), dataType()});
    }
}
