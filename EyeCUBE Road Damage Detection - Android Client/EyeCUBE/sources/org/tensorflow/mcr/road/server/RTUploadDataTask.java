package org.tensorflow.mcr.road.server;

import android.content.Context;
import android.os.AsyncTask;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.File;
import java.util.Date;
import java.util.List;
import org.ateam.eyecube.mcr.R;

public class RTUploadDataTask extends AsyncTask<String, Integer, Void> {
    private static final int DELAY_MILLIS = 2000;
    private static final int LOOP_STEP = 5;
    private File dir;
    private long lastTimestamp;
    private String prefixDamage;
    private String prefixLocation;
    private String urlDamage;
    private String urlLocation;

    private void upload() {
        try {
            if (this.dir.exists()) {
                List<File> damages = DataUtils.getListDamageFiles(this.dir, this.prefixDamage);
                if (damages.size() > 0) {
                    int numLoops = damages.size() / 5;
                    if (damages.size() % 5 != 0) {
                        numLoops++;
                    }
                    int i = 1;
                    while (i <= numLoops) {
                        try {
                            List<File> sublist = damages.subList((i - 1) * 5, i != numLoops ? i * 5 : damages.size());
                            if (sublist.size() > 0) {
                                if (DataUtils.uploadJson(this.urlDamage, DataUtils.loadDamages(sublist).toString())) {
                                    DataUtils.eraseFiles(sublist);
                                }
                            }
                        } catch (Exception e) {
                            ThrowableExtension.printStackTrace(e);
                        }
                        i++;
                    }
                }
                try {
                    List<File> locations = DataUtils.getListLocationFiles(this.dir, this.prefixLocation);
                    if (locations.size() > 0) {
                        if (DataUtils.uploadJson(this.urlLocation, DataUtils.loadLocations(locations).toString())) {
                            DataUtils.eraseFiles(locations);
                        }
                    }
                } catch (Exception e2) {
                    ThrowableExtension.printStackTrace(e2);
                }
            }
        } catch (Exception e3) {
            ThrowableExtension.printStackTrace(e3);
        }
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(String... strings) {
        while (!isCancelled()) {
            try {
                long currentTimestamp = new Date().getTime();
                upload();
                long delayTime = 2000 - (currentTimestamp - this.lastTimestamp);
                if (delayTime > 0) {
                    Thread.sleep(delayTime);
                }
                this.lastTimestamp = currentTimestamp;
            } catch (Exception e) {
                ThrowableExtension.printStackTrace(e);
                return null;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onCancelled() {
        super.onCancelled();
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Void aVoid) {
    }

    public void ready(Context context, String phoneCode, File directory) {
        this.dir = directory;
        this.prefixDamage = context.getString(R.string.file_prefix_damage);
        this.prefixLocation = context.getString(R.string.file_prefix_location);
        String host = context.getString(R.string.url_scheme_host);
        String regex = context.getString(R.string.url_regex);
        String apiDamage = context.getString(R.string.url_damage).replace(regex, phoneCode);
        String apiLocation = context.getString(R.string.url_location).replace(regex, phoneCode);
        this.urlDamage = String.format("%s%s", new Object[]{host, apiDamage});
        this.urlLocation = String.format("%s%s", new Object[]{host, apiLocation});
        this.lastTimestamp = 0;
    }
}
