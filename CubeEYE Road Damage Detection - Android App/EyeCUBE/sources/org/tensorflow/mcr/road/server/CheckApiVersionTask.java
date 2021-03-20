package org.tensorflow.mcr.road.server;

import android.os.AsyncTask;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import org.json.JSONObject;
import org.tensorflow.mcr.ITaskDelegate;

public class CheckApiVersionTask extends AsyncTask<String, Void, Void> {
    private ITaskDelegate delegate;
    private String version;

    public CheckApiVersionTask(ITaskDelegate delegate2) {
        this.delegate = delegate2;
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(String... objects) {
        this.version = "";
        try {
            if (!isCancelled()) {
                this.version = new JSONObject(DataUtils.downloadString(objects[0])).getString("version");
            }
        } catch (Exception e) {
            ThrowableExtension.printStackTrace(e);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Void aVoid) {
        this.delegate.complete(this.version);
    }
}
