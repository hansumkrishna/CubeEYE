package org.tensorflow;

import java.util.Iterator;

public final class Graph implements AutoCloseable {
    /* access modifiers changed from: private */
    public long nativeHandle;
    /* access modifiers changed from: private */
    public final Object nativeHandleLock;
    private int refcount;

    private static native long allocate();

    private static native void delete(long j);

    private static native void importGraphDef(long j, byte[] bArr, String str) throws IllegalArgumentException;

    /* access modifiers changed from: private */
    public static native long[] nextOperation(long j, int i);

    private static native long operation(long j, String str);

    private static native byte[] toGraphDef(long j);

    static /* synthetic */ int access$206(Graph x0) {
        int i = x0.refcount - 1;
        x0.refcount = i;
        return i;
    }

    static /* synthetic */ int access$208(Graph x0) {
        int i = x0.refcount;
        x0.refcount = i + 1;
        return i;
    }

    public Graph() {
        this.nativeHandleLock = new Object();
        this.refcount = 0;
        this.nativeHandle = allocate();
    }

    Graph(long nativeHandle2) {
        this.nativeHandleLock = new Object();
        this.refcount = 0;
        this.nativeHandle = nativeHandle2;
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r8 = this;
            r6 = 0
            java.lang.Object r2 = r8.nativeHandleLock
            monitor-enter(r2)
            long r4 = r8.nativeHandle     // Catch:{ all -> 0x0021 }
            int r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r1 != 0) goto L_0x000d
            monitor-exit(r2)     // Catch:{ all -> 0x0021 }
        L_0x000c:
            return
        L_0x000d:
            int r1 = r8.refcount     // Catch:{ all -> 0x0021 }
            if (r1 <= 0) goto L_0x0024
            java.lang.Object r1 = r8.nativeHandleLock     // Catch:{ InterruptedException -> 0x0017 }
            r1.wait()     // Catch:{ InterruptedException -> 0x0017 }
            goto L_0x000d
        L_0x0017:
            r0 = move-exception
            java.lang.Thread r1 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0021 }
            r1.interrupt()     // Catch:{ all -> 0x0021 }
            monitor-exit(r2)     // Catch:{ all -> 0x0021 }
            goto L_0x000c
        L_0x0021:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0021 }
            throw r1
        L_0x0024:
            long r4 = r8.nativeHandle     // Catch:{ all -> 0x0021 }
            delete(r4)     // Catch:{ all -> 0x0021 }
            r4 = 0
            r8.nativeHandle = r4     // Catch:{ all -> 0x0021 }
            monitor-exit(r2)     // Catch:{ all -> 0x0021 }
            goto L_0x000c
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Graph.close():void");
    }

    public Operation operation(String name) {
        Operation operation;
        synchronized (this.nativeHandleLock) {
            long oph = operation(this.nativeHandle, name);
            if (oph == 0) {
                operation = null;
            } else {
                operation = new Operation(this, oph);
            }
        }
        return operation;
    }

    public Iterator<Operation> operations() {
        return new OperationIterator(this);
    }

    public OperationBuilder opBuilder(String type, String name) {
        return new OperationBuilder(this, type, name);
    }

    public void importGraphDef(byte[] graphDef) throws IllegalArgumentException {
        importGraphDef(graphDef, "");
    }

    public void importGraphDef(byte[] graphDef, String prefix) throws IllegalArgumentException {
        if (graphDef == null || prefix == null) {
            throw new IllegalArgumentException("graphDef and prefix cannot be null");
        }
        synchronized (this.nativeHandleLock) {
            importGraphDef(this.nativeHandle, graphDef, prefix);
        }
    }

    public byte[] toGraphDef() {
        byte[] graphDef;
        synchronized (this.nativeHandleLock) {
            graphDef = toGraphDef(this.nativeHandle);
        }
        return graphDef;
    }

    class Reference implements AutoCloseable {
        private boolean active;
        final /* synthetic */ Graph this$0;

        private Reference(Graph this$02) {
            boolean z = true;
            this.this$0 = this$02;
            synchronized (this$02.nativeHandleLock) {
                this.active = this$02.nativeHandle == 0 ? false : z;
                if (!this.active) {
                    throw new IllegalStateException("close() has been called on the Graph");
                }
                this.active = true;
                Graph.access$208(this$02);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() {
            /*
                r2 = this;
                org.tensorflow.Graph r0 = r2.this$0
                java.lang.Object r1 = r0.nativeHandleLock
                monitor-enter(r1)
                boolean r0 = r2.active     // Catch:{ all -> 0x0023 }
                if (r0 != 0) goto L_0x000d
                monitor-exit(r1)     // Catch:{ all -> 0x0023 }
            L_0x000c:
                return
            L_0x000d:
                r0 = 0
                r2.active = r0     // Catch:{ all -> 0x0023 }
                org.tensorflow.Graph r0 = r2.this$0     // Catch:{ all -> 0x0023 }
                int r0 = org.tensorflow.Graph.access$206(r0)     // Catch:{ all -> 0x0023 }
                if (r0 != 0) goto L_0x0021
                org.tensorflow.Graph r0 = r2.this$0     // Catch:{ all -> 0x0023 }
                java.lang.Object r0 = r0.nativeHandleLock     // Catch:{ all -> 0x0023 }
                r0.notifyAll()     // Catch:{ all -> 0x0023 }
            L_0x0021:
                monitor-exit(r1)     // Catch:{ all -> 0x0023 }
                goto L_0x000c
            L_0x0023:
                r0 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x0023 }
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Graph.Reference.close():void");
        }

        public long nativeHandle() {
            long access$100;
            synchronized (this.this$0.nativeHandleLock) {
                access$100 = this.active ? this.this$0.nativeHandle : 0;
            }
            return access$100;
        }
    }

    /* access modifiers changed from: package-private */
    public Reference ref() {
        return new Reference();
    }

    private static final class OperationIterator implements Iterator<Operation> {
        private final Graph graph;
        private Operation operation = null;
        private int position = 0;

        OperationIterator(Graph g) {
            this.graph = g;
            advance();
        }

        private final void advance() {
            Reference reference = this.graph.ref();
            this.operation = null;
            try {
                long[] nativeReturn = Graph.nextOperation(reference.nativeHandle(), this.position);
                if (!(nativeReturn == null || nativeReturn[0] == 0)) {
                    this.operation = new Operation(this.graph, nativeReturn[0]);
                    this.position = (int) nativeReturn[1];
                }
            } finally {
                reference.close();
            }
        }

        public boolean hasNext() {
            return this.operation != null;
        }

        public Operation next() {
            Operation rhett = this.operation;
            advance();
            return rhett;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() is unsupported.");
        }
    }

    static {
        TensorFlow.init();
    }
}
