package org.tensorflow.mcr.road;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import org.tensorflow.mcr.ITaskDelegate;
import org.tensorflow.mcr.road.server.DataUtils;
import org.tensorflow.mcr.road.server.UploadDataTask;
import org.ateam.eyecube.mcr.R;

public class UploadActivity extends Activity implements ITaskDelegate {
    private String phoneCode;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcr_activity_upload);
        this.phoneCode = getIntent().getStringExtra("phoneCode");
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        ((Button) findViewById(R.id.btn_upload)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.setTitle("upload");
                dialog.setMessage("Are you sure you want to upload ?");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UploadActivity.this.uploadFiles();
                    }
                });
                dialog.setNegativeButton("Cancel", (DialogInterface.OnClickListener) null);
                dialog.show();
            }
        });
        updateListViewItems();
    }

    private void updateListViewItems() {
        ListView listView = (ListView) findViewById(R.id.list_items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 17367043);
        File dir = getExternalFilesDir((String) null);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    adapter.add(String.format("%s(%d)", new Object[]{file.getName(), Integer.valueOf(file.list().length)}));
                }
            }
        }
        listView.setAdapter(adapter);
        ((TextView) findViewById(R.id.txtMessage)).setText(adapter.getCount() <= 0 ? getString(R.string.message_no_files) : "");
    }

    /* access modifiers changed from: private */
    public void uploadFiles() {
        File dir = getExternalFilesDir((String) null);
        if (dir.exists()) {
            String prefixDamage = getString(R.string.file_prefix_damage);
            String prefixLocation = getString(R.string.file_prefix_location);
            new UploadDataTask(this, this.phoneCode, DataUtils.getListDamageFiles(dir, prefixDamage), DataUtils.getListLocationFiles(dir, prefixLocation), this).execute(new String[0]);
        }
    }

    public void complete(Object result) {
        DataUtils.eraseDirctory(getExternalFilesDir((String) null));
        updateListViewItems();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        startActivity(new Intent(getApplication(), RoadDamageDetectorActivity.class));
    }
}
