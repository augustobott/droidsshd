/**
 * 
 */
package br.com.bott.droidsshd.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import br.com.bott.droidsshd.R;
import br.com.bott.droidsshd.system.Base;
import br.com.bott.droidsshd.system.Util;
import br.com.bott.droidsshd.tools.ShellSession;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author mestre
 * 
 */
public class InitialSetup extends Activity {

	private static final String TAG = "DroidSSHd-Setup";
	protected ProgressDialog dialog;

	private Button buttonOk;
	private Button buttonCancel;
	private TextView textInitialSetup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Base.getContext() == null) {
			Base.initialize(getBaseContext());
		} else {
			Base.refresh();
		}
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setup);
		if (Base.debug) {
			Log.d(TAG, "onCreate() called...");
		}
		
		textInitialSetup = (TextView) findViewById(R.id.text_initial_setup);
		
		buttonOk = (Button) findViewById(R.id.button_initial_setup_ok);
		buttonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doInitialSetupDone();
			}
		});

		buttonCancel = (Button) findViewById(R.id.button_initial_setup_cancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, getIntent());
				finish();
//				finishActivity(R.string.activity_initial_setup);
			}
		});
	}

/*	@Override
	protected void onResume() {
		super.onResume();
		Base.refresh();
		if (Base.debug) {
			Log.d(TAG, "onResume() called...");
		}
	}
*/
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int step = msg.getData().getInt("step");
			String text = msg.getData().getString("text");
			dialog.setProgress(step);
			dialog.setMessage(text);
			if (step >= dialog.getMax()) {
				dialog.dismiss();
			}
		}
	};

	protected class ProgressThread extends Thread {
		Handler h;
		int step;
		int total;

		ProgressThread(Handler h) {
			this.h = h;
		}

		public void run() {
			Message message = null;
			Bundle data = new Bundle();

			message = new Message();
			data.putString("text", "Creating directories");
			data.putInt("step", 1);
			message.setData(data);
			h.sendMessage(message);
//			Util.mysleep(200);
			setupDirectoryStructure();

			message = new Message();
			data.putString("text", "Deploying dropbear binaries");
			data.putInt("step", 2);
			message.setData(data);
			h.sendMessage(message);
//			Util.mysleep(200);
			deployDropbearBinaries();

			message = new Message();
			data.putString("text", "Generating DSS host key. This may take a while...");
			data.putInt("step", 3);
			message.setData(data);
			h.sendMessage(message);
//			Util.mysleep(200);
			generateHostKey(Base.DROPBEAR_DSS_HOST_KEY);

			message = new Message();
			data.putString("text", "Generating RSA host key. This may take a while...");
			data.putInt("step", 4);
			message.setData(data);
			h.sendMessage(message);
//			Util.mysleep(500);
			generateHostKey(Base.DROPBEAR_RSA_HOST_KEY);

			message = new Message();
			data.putString("text", "Done. Launching preferences...");
			data.putInt("step", 5);
			message.setData(data);
			h.sendMessage(message);
//			Util.mysleep(200);

			weAreInFactDone();
		}
	}

	protected void doInitialSetupDone() {
		if (Base.debug) {
			Log.d(TAG, "doInitialSetupDone() called...");
		}
		dialog = new ProgressDialog(InitialSetup.this);
		dialog.setMessage("Performing initial setup...");
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setCancelable(false);
		dialog.setMax(5);
		dialog.show();

		buttonOk.setVisibility(View.INVISIBLE);
		buttonCancel.setVisibility(View.INVISIBLE);
		textInitialSetup.setVisibility(View.INVISIBLE);

		ProgressThread pt = new ProgressThread(handler);
		pt.start();
	}

	protected void weAreInFactDone() {
		// dialog.dismiss();
		// finish();
		setResult(RESULT_OK, getIntent());
		finishActivity(R.string.activity_initial_setup);
		Intent p = new Intent(Base.getContext(), br.com.bott.droidsshd.activity.Preferences.class);
		startActivity(p);
	}

	protected boolean setupDirectoryStructure() {
		// files/bin - binaries
		Util.mkdir(Base.getDropbearBinDirPath());
		Util.chmod(Base.getDropbearBinDirPath(), 0755);
		// files/run - pidfile
		Util.mkdir(Base.getDropbearTmpDirPath());
		Util.chmod(Base.getDropbearTmpDirPath(), 0775);
		// files/etc - authorized pubkeys and host keys
		Util.mkdir(Base.getDropbearEtcDirPath());
		Util.chmod(Base.getDropbearEtcDirPath(), 0700);
		return true;
	}

	protected boolean deployDropbearBinaries() {
		String path = Base.getDropbearBinDirPath() + "/" + Base.DROPBEAR_BIN_MUL;
		File dest = new File(path);
		if (dest.exists()) {
			Log.e(TAG, "deployDropbearBinaries: " + path + " already exists!");
			// TODO - dropbearmulti might be there, but... are symlinks/permissions set?
			// TODO - yeah, yeah - it's highly unlikely... so probably WONTFIX
			return false;
		}
		try {
			InputStream is = getAssets().open(Base.DROPBEAR_BIN_MUL);
			FileOutputStream os = new FileOutputStream(path);
			byte[] buffer = new byte[4096];
			int count;
			while ((count = is.read(buffer)) != -1) {
				os.write(buffer, 0, count);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception when copying asset dropbearmulti: ", e);
			e.printStackTrace();
			return false;
		}
		Util.chmod(path, 0755);
		Util.symlink(path, Base.getDropbearBinDirPath() + "/"
				+ Base.DROPBEAR_BIN_SRV);
		Util.symlink(path, Base.getDropbearBinDirPath() + "/"
				+ Base.DROPBEAR_BIN_KEY);
		Util.symlink(path, Base.getDropbearBinDirPath() + "/"
				+ Base.DROPBEAR_BIN_SCP);
		return true;
	}

	public boolean generateHostKey(String which) {
		String path = "";
		String type = "none";
		if (which == Base.DROPBEAR_DSS_HOST_KEY) {
			type = "dss";
			path = Base.getDropbearDssHostKeyFilePath();
		}
		if (which == Base.DROPBEAR_RSA_HOST_KEY) {
			type = "rsa";
			path = Base.getDropbearRsaHostKeyFilePath();
		}
		if (type == "none") {
			Log.e(TAG,
					"generateHostKey has been asked to create an unknown type of key: " + which);
			return false;
		}
		String cmd = Base.getDropbearBinDirPath() + "/" + Base.DROPBEAR_BIN_KEY
				+ " -t " + type + " -f " + path;
		if (Base.debug) {
			Log.v(TAG, "generateHostKey('" + which + "'), keyType = " + type);
			Log.v(TAG, "cmd = '" + cmd + "'");
		}
		ShellSession p = new ShellSession(TAG + "-shell", cmd, false,
				Base.debug);
		
		try {
			p.start();
			p.join();
			p.waitFor();
		} catch (InterruptedException e) {
//			TODO - do we really care? :-)
//			TODO - I mean: host keys are 'checked' in lots of places already :-)
// 			TODO - yeah, yeah - it's highly unlikely... so probably WONTFIX
//			e.printStackTrace();
		}
		return true;
	}

}
