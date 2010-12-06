/**
 * 
 */
package br.com.bott.droidsshd.activity;

import java.io.File;
import br.com.bott.droidsshd.R;
import br.com.bott.droidsshd.system.Base;
import br.com.bott.droidsshd.system.Util;
import br.com.bott.droidsshd.tools.NumberPickerPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.util.Log;

/**
 * @author mestre
 *
 */
public class Preferences extends PreferenceActivity 
	implements OnSharedPreferenceChangeListener {
	private static final String TAG = "DroidSSHd-Prefs";
	private CheckBoxPreference mDebug;
//	private EditTextPreference mPort;
	protected NumberPickerPreference mPort;
	private Preference mAuthorizedKeys;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);
		mDebug = (CheckBoxPreference) findPreference(getString(R.string.pref_debug_key));
		if (mDebug.isChecked()) {
			Log.v(TAG, "onCreate called");
		}
//		mPort = (EditTextPreference) findPreference(getString(R.string.pref_dropbear_port_key));
//		mPort.getEditText().setKeyListener(new DialerKeyListener());
		mPort = (NumberPickerPreference) findPreference(getString(R.string.pref_dropbear_port_key));

		mAuthorizedKeys = findPreference("authorized_keys_key");
		mAuthorizedKeys.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
					Intent p = new Intent(getBaseContext(), com.h3r3t1c.filechooser.FileChooser.class);
					startActivityForResult(p, R.string.activity_file_chooser);
					return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mDebug.isChecked()) {
			Log.v(TAG, "onResume called");
		}
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
//		mPort.setSummary(getString(R.string.pref_dropbear_port_summary) + " " + mPort.getText());
		mPort.setSummary(getString(R.string.pref_dropbear_port_summary) + " " + mPort.getValue());
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);	
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, br.com.bott.droidsshd.DroidSSHd.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
//		super.onBackPressed();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(mDebug.isChecked()) {
			Log.d(TAG, "onSharedPreferenceChanged called with key " + key);
		}
		if (key.equals(getString(R.string.pref_dropbear_port_key))) {
			mPort.setSummary(getString(R.string.pref_dropbear_port_summary) + " " + mPort.getValue());
		}
//		if (key.equals("authorized_keys_key")){
//			Log.v(TAG, "authorized_keys_key changed: " + key);
//		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mDebug.isChecked()) {
			if (data!=null) {
				Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data.toString() + ") called");
			} else {
				Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", null) called");
			}
		}
		if(resultCode==RESULT_OK){
			if(mDebug.isChecked()){
				Log.v(TAG,"path = " + data.getStringExtra("path"));
			}
			Base.refresh();
			File tmp = new File(data.getStringExtra("path"));
			if(tmp.length()<1024) {
				Util.copyFile(data.getStringExtra("path"), Base.getDropbearAuthorizedKeysFilePath());
				Util.chmod(Base.getDropbearAuthorizedKeysFilePath(), 0600);
				Util.showMsg("Public key copied");
			} else {
				Util.showMsg("The file is too big to be a public key.");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
