package org.tensorflow.mcr.road;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import org.tensorflow.mcr.TaskDelegate;
import org.tensorflow.mcr.road.server.CheckApiVersionTask;
import org.ateam.eyecube.mcr.R;

public class MainActivity extends Activity {
    private static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    private static final String PERMISSION_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    /* access modifiers changed from: private */
    public long fileId;

    public class DownloadReceiver extends BroadcastReceiver {
        public DownloadReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.DOWNLOAD_COMPLETE")) {
                long id = intent.getLongExtra("extra_download_id", -1);
                if (id == MainActivity.this.fileId) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterByStatus(16);
                    query.setFilterByStatus(8);
                    query.setFilterById(new long[]{id});
                    Cursor cursor = ((DownloadManager) context.getSystemService("download")).query(query);
                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex("status"));
                        String url = cursor.getString(cursor.getColumnIndex("local_uri"));
                        if (status == 8) {
                            MainActivity.this.install(context, url);
                        }
                    }
                    cursor.close();
                }
            }
            MainActivity.this.finish();
        }
    }

    /* access modifiers changed from: private */
    public void install(Context context, String url) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
        intent.setFlags(268435457);
        context.startActivity(intent);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcr_activity_main);
        if (!hasPermission()) {
            requestPermission();
        }
        ((TextView) findViewById(R.id.textVersion)).setText(getString(R.string.message_app_check_version));
        ((TextView) findViewById(R.id.txtDeveloper)).setText(getString(R.string.message_developper));
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        String url = String.format("%s%s", new Object[]{getString(R.string.url_scheme_host), getString(R.string.url_application_version)});
        if (getResources().getBoolean(R.bool.closed_mode)) {
            new CheckApiVersionTask(new TaskDelegate() {
                public void complete(Object object) {
                    String latestVersion = (String) object;
                    String localVersion = MainActivity.getVersionName(MainActivity.this.getApplicationContext());
                    if (latestVersion == null || latestVersion.equals("") || latestVersion.equals(localVersion)) {
                        MainActivity.this.startActivity(new Intent(MainActivity.this.getApplication(), RoadDamageDetectorActivity.class));
                        MainActivity.this.finish();
                        return;
                    }
                    MainActivity.this.updateApplicaiton();
                }
            }).execute(new String[]{url});
            return;
        }
        startActivity(new Intent(getApplication(), RoadDamageDetectorActivity.class));
        finish();
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            ThrowableExtension.printStackTrace(e);
            return "";
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 1) {
            return;
        }
        if (grantResults.length <= 0 || grantResults[0] != 0 || grantResults[1] != 0 || grantResults[2] != 0 || grantResults[3] != 0) {
            requestPermission();
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (checkSelfPermission(PERMISSION_CAMERA) == 0 && checkSelfPermission(PERMISSION_STORAGE) == 0 && checkSelfPermission(ACCESS_FINE_LOCATION) == 0 && checkSelfPermission(READ_PHONE_STATE) == 0) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE) || shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE, ACCESS_FINE_LOCATION, READ_PHONE_STATE}, 1);
        }
    }

    /* access modifiers changed from: private */
    public void updateApplicaiton() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(String.format("%s%s", new Object[]{getString(R.string.url_scheme_host), getString(R.string.url_application)})));
        request.setAllowedNetworkTypes(3);
        request.setTitle(getString(R.string.message_app_download));
        request.setVisibleInDownloadsUi(false);
        registerReceiver(new DownloadReceiver(), new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"));
        this.fileId = ((DownloadManager) getSystemService("download")).enqueue(request);
    }
}
