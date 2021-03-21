package org.tensorflow;

public class SavedModelBundle implements AutoCloseable {
    private final Graph graph;
    private final byte[] metaGraphDef;
    private final Session session;

    private static native SavedModelBundle load(String str, String[] strArr, byte[] bArr);

    public static SavedModelBundle load(String exportDir, String... tags) {
        return load(exportDir, tags, (byte[]) null);
    }

    public byte[] metaGraphDef() {
        return this.metaGraphDef;
    }

    public Graph graph() {
        return this.graph;
    }

    public Session session() {
        return this.session;
    }

    public void close() {
        this.session.close();
        this.graph.close();
    }

    private SavedModelBundle(Graph graph2, Session session2, byte[] metaGraphDef2) {
        this.graph = graph2;
        this.session = session2;
        this.metaGraphDef = metaGraphDef2;
    }

    private static SavedModelBundle fromHandle(long graphHandle, long sessionHandle, byte[] metaGraphDef2) {
        Graph graph2 = new Graph(graphHandle);
        return new SavedModelBundle(graph2, new Session(graph2, sessionHandle), metaGraphDef2);
    }

    static {
        TensorFlow.init();
    }
}
