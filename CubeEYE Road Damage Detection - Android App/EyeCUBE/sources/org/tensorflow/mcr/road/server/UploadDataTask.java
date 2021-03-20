package org.tensorflow.mcr.road.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.File;
import java.util.List;
import org.tensorflow.mcr.ITaskDelegate;
import org.ateam.eyecube.mcr.R;

public class UploadDataTask extends AsyncTask<String, Integer, Void> implements DialogInterface.OnCancelListener {
    private static final int LOOP_STEP = 5;
    Context context;
    private List<File> damages;
    protected ITaskDelegate delegate;
    ProgressDialog dialog;
    private List<File> locations;
    private String phoneCode;

    public UploadDataTask(Context context2, String phoneCode2, List<File> damages2, List<File> locations2, ITaskDelegate delegate2) {
        this.context = context2;
        this.phoneCode = phoneCode2;
        this.damages = damages2;
        this.locations = locations2;
        this.delegate = delegate2;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        this.dialog = new ProgressDialog(this.context);
        this.dialog.setTitle("Please wait");
        this.dialog.setMessage("Uploading ...");
        this.dialog.setProgressStyle(1);
        this.dialog.setCancelable(true);
        this.dialog.setOnCancelListener(this);
        this.dialog.setMax((this.damages.size() / 5) + 2);
        this.dialog.setProgress(0);
        this.dialog.show();
    }

    private String getUrl(String target) {
        return String.format("%s%s", new Object[]{this.context.getString(R.string.url_scheme_host), target.replace(this.context.getString(R.string.url_regex), this.phoneCode)});
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(String... objects) {
        int progress = 0;
        int numLoops = this.damages.size() / 5;
        if (this.damages.size() % 5 != 0) {
            numLoops++;
        }
        String urlDamage = getUrl(this.context.getString(R.string.url_damage));
        int i = 1;
        while (i <= numLoops) {
            try {
                if (isCancelled()) {
                    break;
                }
                List<File> sublist = this.damages.subList((i - 1) * 5, i != numLoops ? i * 5 : this.damages.size());
                if (DataUtils.uploadJson(urlDamage, DataUtils.loadDamages(sublist).toString())) {
                    DataUtils.eraseFiles(sublist);
                }
                progress++;
                publishProgress(new Integer[]{Integer.valueOf(progress)});
                i++;
            } catch (Exception e) {
                ThrowableExtension.printStackTrace(e);
            }
        }
        if (isCancelled()) {
            return null;
        }
        try {
            if (DataUtils.uploadJson(getUrl(this.context.getString(R.string.url_location)), DataUtils.loadLocations(this.locations).toString())) {
                DataUtils.eraseFiles(this.locations);
            }
            publishProgress(new Integer[]{Integer.valueOf(progress + 1)});
        } catch (Exception e2) {
            ThrowableExtension.printStackTrace(e2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onProgressUpdate(Integer... values) {
        this.dialog.setProgress(values[0].intValue());
    }

    /* access modifiers changed from: protected */
    public void onCancelled() {
        this.dialog.dismiss();
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Void aVoid) {
        this.dialog.dismiss();
        this.delegate.complete(0);
    }

    public void onCancel(DialogInterface dialog2) {
        cancel(true);
    }
}
