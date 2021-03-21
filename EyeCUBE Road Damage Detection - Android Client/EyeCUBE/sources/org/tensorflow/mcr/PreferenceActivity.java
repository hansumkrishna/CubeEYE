package org.tensorflow.mcr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import org.ateam.eyecube.mcr.R;

public class PreferenceActivity extends Activity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcr_activity_preference);
        getFragmentManager().beginTransaction().replace(16908290, new PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static final int CHOSE_FILE_CODE = 12345;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            EditTextPreference textPhone = (EditTextPreference) findPreference(getString(R.string.preference_phoneid_id));
            textPhone.setSummary(textPhone.getText());
            EditTextPreference textThreshold = (EditTextPreference) findPreference(getString(R.string.preference_threthold_id));
            textThreshold.setSummary(textThreshold.getText());
            findPreference(getString(R.string.preference_model_id)).setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.preference_model_id), ""));
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (!preference.getKey().equals(getString(R.string.preference_model_id))) {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("file/*");
            startActivityForResult(intent, CHOSE_FILE_CODE);
            return false;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == CHOSE_FILE_CODE) {
                String path = "";
                if (resultCode == -1) {
                    try {
                        Cursor cursor = getActivity().getContentResolver().query(data.getData(), new String[]{"_data"}, (String) null, (String[]) null, (String) null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            path = cursor.getString(0);
                        }
                        cursor.close();
                    } catch (Exception e) {
                        ThrowableExtension.printStackTrace(e);
                        return;
                    }
                }
                SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                editor.putString(getString(R.string.preference_model_id), path);
                editor.apply();
                findPreference(getString(R.string.preference_model_id)).setSummary(path);
            }
        }

        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            ((EditTextPreference) findPreference(key)).setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
