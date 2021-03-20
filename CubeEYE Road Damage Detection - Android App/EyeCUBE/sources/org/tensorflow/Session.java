package org.tensorflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.tensorflow.Graph;

public final class Session implements AutoCloseable {
    /* access modifiers changed from: private */
    public final Graph graph;
    private final Graph.Reference graphRef;
    /* access modifiers changed from: private */
    public long nativeHandle;
    /* access modifiers changed from: private */
    public final Object nativeHandleLock;
    private int numActiveRuns;

    public static final class Run {
        public byte[] metadata;
        public List<Tensor<?>> outputs;
    }

    private static native long allocate(long j);

    private static native long allocate2(long j, String str, byte[] bArr);

    private static native void delete(long j);

    /* access modifiers changed from: private */
    public static native byte[] run(long j, byte[] bArr, long[] jArr, long[] jArr2, int[] iArr, long[] jArr3, int[] iArr2, long[] jArr4, boolean z, long[] jArr5);

    static /* synthetic */ int access$304(Session x0) {
        int i = x0.numActiveRuns + 1;
        x0.numActiveRuns = i;
        return i;
    }

    static /* synthetic */ int access$306(Session x0) {
        int i = x0.numActiveRuns - 1;
        x0.numActiveRuns = i;
        return i;
    }

    public Session(Graph g) {
        this(g, (byte[]) null);
    }

    public Session(Graph g, byte[] config) {
        long allocate2;
        this.nativeHandleLock = new Object();
        this.graph = g;
        Graph.Reference r = g.ref();
        if (config == null) {
            try {
                allocate2 = allocate(r.nativeHandle());
            } catch (Throwable th) {
                r.close();
                throw th;
            }
        } else {
            allocate2 = allocate2(r.nativeHandle(), (String) null, config);
        }
        this.nativeHandle = allocate2;
        this.graphRef = g.ref();
        r.close();
    }

    Session(Graph g, long nativeHandle2) {
        this.nativeHandleLock = new Object();
        this.graph = g;
        this.nativeHandle = nativeHandle2;
        this.graphRef = g.ref();
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r8 = this;
            r6 = 0
            org.tensorflow.Graph$Reference r1 = r8.graphRef
            r1.close()
            java.lang.Object r2 = r8.nativeHandleLock
            monitor-enter(r2)
            long r4 = r8.nativeHandle     // Catch:{ all -> 0x0026 }
            int r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r1 != 0) goto L_0x0012
            monitor-exit(r2)     // Catch:{ all -> 0x0026 }
        L_0x0011:
            return
        L_0x0012:
            int r1 = r8.numActiveRuns     // Catch:{ all -> 0x0026 }
            if (r1 <= 0) goto L_0x0029
            java.lang.Object r1 = r8.nativeHandleLock     // Catch:{ InterruptedException -> 0x001c }
            r1.wait()     // Catch:{ InterruptedException -> 0x001c }
            goto L_0x0012
        L_0x001c:
            r0 = move-exception
            java.lang.Thread r1 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0026 }
            r1.interrupt()     // Catch:{ all -> 0x0026 }
            monitor-exit(r2)     // Catch:{ all -> 0x0026 }
            goto L_0x0011
        L_0x0026:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0026 }
            throw r1
        L_0x0029:
            long r4 = r8.nativeHandle     // Catch:{ all -> 0x0026 }
            delete(r4)     // Catch:{ all -> 0x0026 }
            r4 = 0
            r8.nativeHandle = r4     // Catch:{ all -> 0x0026 }
            monitor-exit(r2)     // Catch:{ all -> 0x0026 }
            goto L_0x0011
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Session.close():void");
    }

    public final class Runner {
        private ArrayList<Tensor<?>> inputTensors = new ArrayList<>();
        private ArrayList<Output<?>> inputs = new ArrayList<>();
        private ArrayList<Output<?>> outputs = new ArrayList<>();
        private byte[] runOptions = null;
        private ArrayList<Operation> targets = new ArrayList<>();

        public Runner() {
        }

        public Runner feed(String operation, Tensor<?> t) {
            return feed(parseOutput(operation), t);
        }

        public Runner feed(String operation, int index, Tensor<?> t) {
            Operation op = operationByName(operation);
            if (op != null) {
                this.inputs.add(op.output(index));
                this.inputTensors.add(t);
            }
            return this;
        }

        public Runner feed(Output<?> o, Tensor<?> t) {
            this.inputs.add(o);
            this.inputTensors.add(t);
            return this;
        }

        public Runner fetch(String operation) {
            return fetch(parseOutput(operation));
        }

        public Runner fetch(String operation, int index) {
            Operation op = operationByName(operation);
            if (op != null) {
                this.outputs.add(op.output(index));
            }
            return this;
        }

        public Runner fetch(Output<?> output) {
            this.outputs.add(output);
            return this;
        }

        public Runner addTarget(String operation) {
            Operation op = operationByName(operation);
            if (op != null) {
                this.targets.add(op);
            }
            return this;
        }

        public Runner addTarget(Operation operation) {
            this.targets.add(operation);
            return this;
        }

        public Runner setOptions(byte[] options) {
            this.runOptions = options;
            return this;
        }

        public List<Tensor<?>> run() {
            return runHelper(false).outputs;
        }

        public Run runAndFetchMetadata() {
            return runHelper(true);
        }

        /* JADX INFO: finally extract failed */
        private Run runHelper(boolean wantMetadata) {
            long[] inputTensorHandles = new long[this.inputTensors.size()];
            long[] inputOpHandles = new long[this.inputs.size()];
            int[] inputOpIndices = new int[this.inputs.size()];
            long[] outputOpHandles = new long[this.outputs.size()];
            int[] outputOpIndices = new int[this.outputs.size()];
            long[] targetOpHandles = new long[this.targets.size()];
            long[] outputTensorHandles = new long[this.outputs.size()];
            int idx = 0;
            Iterator<Tensor<?>> it = this.inputTensors.iterator();
            while (it.hasNext()) {
                inputTensorHandles[idx] = it.next().getNativeHandle();
                idx++;
            }
            int idx2 = 0;
            Iterator<Output<?>> it2 = this.inputs.iterator();
            while (it2.hasNext()) {
                Output<?> o = it2.next();
                inputOpHandles[idx2] = o.mo6082op().getUnsafeNativeHandle();
                inputOpIndices[idx2] = o.index();
                idx2++;
            }
            int idx3 = 0;
            Iterator<Output<?>> it3 = this.outputs.iterator();
            while (it3.hasNext()) {
                Output<?> o2 = it3.next();
                outputOpHandles[idx3] = o2.mo6082op().getUnsafeNativeHandle();
                outputOpIndices[idx3] = o2.index();
                idx3++;
            }
            int idx4 = 0;
            Iterator<Operation> it4 = this.targets.iterator();
            while (it4.hasNext()) {
                targetOpHandles[idx4] = it4.next().getUnsafeNativeHandle();
                idx4++;
            }
            Reference reference = new Reference();
            try {
                byte[] metadata = Session.run(Session.this.nativeHandle, this.runOptions, inputTensorHandles, inputOpHandles, inputOpIndices, outputOpHandles, outputOpIndices, targetOpHandles, wantMetadata, outputTensorHandles);
                reference.close();
                List<Tensor<?>> outputs2 = new ArrayList<>();
                int length = outputTensorHandles.length;
                int i = 0;
                while (i < length) {
                    try {
                        outputs2.add(Tensor.fromHandle(outputTensorHandles[i]));
                        i++;
                    } catch (Exception e) {
                        for (Tensor<?> t : outputs2) {
                            t.close();
                        }
                        outputs2.clear();
                        throw e;
                    }
                }
                Run ret = new Run();
                ret.outputs = outputs2;
                ret.metadata = metadata;
                return ret;
            } catch (Throwable th) {
                reference.close();
                throw th;
            }
        }

        private class Reference implements AutoCloseable {
            public Reference() {
                synchronized (Session.this.nativeHandleLock) {
                    if (Session.this.nativeHandle == 0) {
                        throw new IllegalStateException("run() cannot be called on the Session after close()");
                    }
                    Session.access$304(Session.this);
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
                return;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void close() {
                /*
                    r6 = this;
                    org.tensorflow.Session$Runner r0 = org.tensorflow.Session.Runner.this
                    org.tensorflow.Session r0 = org.tensorflow.Session.this
                    java.lang.Object r1 = r0.nativeHandleLock
                    monitor-enter(r1)
                    org.tensorflow.Session$Runner r0 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r0 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    long r2 = r0.nativeHandle     // Catch:{ all -> 0x0030 }
                    r4 = 0
                    int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                    if (r0 != 0) goto L_0x0019
                    monitor-exit(r1)     // Catch:{ all -> 0x0030 }
                L_0x0018:
                    return
                L_0x0019:
                    org.tensorflow.Session$Runner r0 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r0 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    int r0 = org.tensorflow.Session.access$306(r0)     // Catch:{ all -> 0x0030 }
                    if (r0 != 0) goto L_0x002e
                    org.tensorflow.Session$Runner r0 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r0 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    java.lang.Object r0 = r0.nativeHandleLock     // Catch:{ all -> 0x0030 }
                    r0.notifyAll()     // Catch:{ all -> 0x0030 }
                L_0x002e:
                    monitor-exit(r1)     // Catch:{ all -> 0x0030 }
                    goto L_0x0018
                L_0x0030:
                    r0 = move-exception
                    monitor-exit(r1)     // Catch:{ all -> 0x0030 }
                    throw r0
                */
                throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Session.Runner.Reference.close():void");
            }
        }

        private Operation operationByName(String opName) {
            Operation op = Session.this.graph.operation(opName);
            if (op != null) {
                return op;
            }
            throw new IllegalArgumentException("No Operation named [" + opName + "] in the Graph");
        }

        private Output<?> parseOutput(String opName) {
            int colon = opName.lastIndexOf(58);
            if (colon == -1 || colon == opName.length() - 1) {
                return new Output<>(operationByName(opName), 0);
            }
            try {
                String op = opName.substring(0, colon);
                return new Output<>(operationByName(op), Integer.parseInt(opName.substring(colon + 1)));
            } catch (NumberFormatException e) {
                return new Output<>(operationByName(opName), 0);
            }
        }
    }

    public Runner runner() {
        return new Runner();
    }
}
