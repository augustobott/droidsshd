/**
 *  This file is part of DroidSSHd.
 *  http://code.google.com/p/droidsshd
 *  
 *  DroidSSHd is open source software: you can redistribute it and/or modify
 *  it under the terms of the Apache License 2.0
 *  
 *  DroidSSHd is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 

 *  @author Augusto Bott (mestre) <augusto@bott.com.br>
 */

package br.com.bott.droidsshd;

import br.com.bott.droidsshd.system.*;
import br.com.bott.droidsshd.tools.*;

import java.util.Iterator;
import android.util.Log;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
//import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
//import android.widget.ScrollView;

public class DroidSSHd extends Activity { 
	private static final String TAG = "DroidSSHd";

// http://developer.android.com/reference/java/lang/Thread.html#setDaemon%28boolean%29

//	http://stackoverflow.com/questions/3011604/how-do-i-get-preferences-to-work-in-android
//	http://stackoverflow.com/questions/2535132/how-do-you-validate-the-format-and-values-of-edittextpreference-entered-in-androi
	private Button btnStartStop;
	private EditText status_content;
	private EditText status_ip_address;
	private EditText status_username;
	private EditText status_tcp_port;
	private CheckBox status_daemon_running_as_root;
	
	private Button preferences_button;

	private ReplicantThread mMonitorDaemon;

	private Intent mDropbearDaemonHandlerService;
	
	private DroidSSHdService mBoundDaemonHandlerService;
	private boolean mDaemonHandlerIsBound;

	private long mUpdateUIdelay = 500L;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Base.initialize(getBaseContext());
		setContentView(R.layout.act_main);

		Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);

		setUpUiListeners();

		mDropbearDaemonHandlerService = new Intent(this, br.com.bott.droidsshd.system.DroidSSHdService.class);

		if ((!Util.validateHostKeys() || (!Util.checkPathToBinaries()))) {
			startInitialSetupActivity();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindDaemonHandlerService(mDropbearDaemonHandlerService);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Base.refresh();
		mHandler.postDelayed(mUpdateUI, mUpdateUIdelay);
//		updateStatus();
		if(Base.debug) {
			Log.d(TAG, "onResume() called");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindDaemonHandlerService(mDropbearDaemonHandlerService);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Base.debug) {
			if (data!=null) {
				Log.v(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data.toString() + ") called");
			} else {
				Log.v(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", null) called");
			}
		}
		if(resultCode==RESULT_CANCELED && requestCode==R.string.activity_initial_setup){
			Util.showMsg("DroidSSHd setup canceled");
			this.finish();
		}
/*		if (requestCode==R.string.activity_file_chooser) {
			if(Base.debug) {
				Log.v(TAG, "FileChooser is done");
			}
			if(resultCode==RESULT_OK){
				if(Base.debug){
					Log.v(TAG,"path = " + data.getStringExtra("path"));
				}
			}
		}*/
	}

	protected void startInitialSetupActivity() {
		Util.showMsg("Initial/basic setup required");
		Intent setup = new Intent(this, br.com.bott.droidsshd.activity.InitialSetup.class);
		setup.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//		setup.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivityForResult(setup, R.string.activity_initial_setup);
	}

	protected void setUpUiListeners() {
		status_content = (EditText) findViewById(R.id.status_content);
		status_ip_address = (EditText) findViewById(R.id.status_ip_address); 
		status_username = (EditText) findViewById(R.id.status_username);
		status_tcp_port = (EditText) findViewById(R.id.status_tcp_port);
		status_daemon_running_as_root  = (CheckBox) findViewById(R.id.status_daemon_running_as_root);
		
		btnStartStop = (Button) findViewById(R.id.status_button);
		btnStartStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnStartStop.setEnabled(false);
				btnStartStop.setFocusable(false);
				btnStartStop.setText("Working");
				if (Util.isDropbearDaemonRunning()) {
						if(Base.debug) {
							Log.v(TAG, "btnStartStop pressed: stopping");
						}
						stopDropbear();
					} else {
						if(Base.debug) {
							Log.v(TAG, "btnStartStop pressed: starting");
						}
						startDropbear();
					}
//				setResult(android.app.Activity.RESULT_OK);
			}
		});

		
//		mStdOut = (EditText) findViewById(R.id.stdout);
//		mLogView = (ScrollView) findViewById(R.id.stdout_scrollview);
		
/*		filechooser_button = (Button) findViewById(R.id.filechooser_button);
		filechooser_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent p = new Intent(v.getContext(), com.h3r3t1c.filechooser.FileChooser.class);
				startActivityForResult(p, R.string.activity_file_chooser);
			}
		});*/

		preferences_button = (Button) findViewById(R.id.preferences_button);
		preferences_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent p = new Intent(v.getContext(), br.com.bott.droidsshd.activity.Preferences.class);
				startActivity(p);
			}
		});
	}

	public void updateStatus() {
		String tmp = "";
		Iterator<String> ipAddr = Util.getLocalIpAddress();
		while(ipAddr.hasNext()) {
			tmp = tmp + ipAddr.next() + " ";
			if (ipAddr.hasNext()) {
				tmp = tmp + ", ";
			}
		}
		status_ip_address.setText(tmp);
		status_username.setText(Base.getUsername());
		status_tcp_port.setText(String.valueOf(Base.getDaemonPort()));
		status_daemon_running_as_root.setChecked(Base.runDaemonAsRoot());
		
		if (Util.isDropbearDaemonRunning()) {
			Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTED);
		}
		switch (Base.getDropbearDaemonStatus()) {
		case Base.DAEMON_STATUS_STOPPING:
			btnStartStop.setEnabled(false);
			btnStartStop.setFocusable(false);
			btnStartStop.setText("Stopping");
			status_content.setText("Working");
			break;

		case Base.DAEMON_STATUS_STARTING:
			btnStartStop.setEnabled(false);
			btnStartStop.setFocusable(false);
			btnStartStop.setText("Starting");
			status_content.setText("Working");
			break;

		case Base.DAEMON_STATUS_STARTED:
			btnStartStop.setEnabled(true);
			btnStartStop.setFocusable(true);
			btnStartStop.setText("Stop");
			status_content.setText("Running");
			break;

		case Base.DAEMON_STATUS_STOPPED:
			btnStartStop.setEnabled(true);
			btnStartStop.setFocusable(true);
			btnStartStop.setText("Start");
			status_content.setText("Stopped");
			break;

		default:
			break;
		}
	}

	public void startDropbear() {
		if (!Util.checkPathToBinaries()) {
			if(Base.debug) {
				Log.v(TAG, "startDropbear bailing out: status was " + Base.getDropbearDaemonStatus() + ", changed to STOPPED(" + ")" );
			}
			Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
			mHandler.postDelayed(mUpdateUI, mUpdateUIdelay);
			Util.showMsg("Can't find dropbear binaries");
			return;
		}
		if (!Util.validateHostKeys()) {
			if(Base.debug) {
				Log.v(TAG, "Host keys not found");
			}
			Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
			mHandler.postDelayed(mUpdateUI, mUpdateUIdelay);
			Util.showMsg("Host keys not found");
			return;
		}
		if(Base.getDropbearDaemonStatus() == Base.DAEMON_STATUS_STOPPED) {
			Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTING);
			if (Base.debug) {
				Log.d(TAG, "Status was STOPPED, now it's STARTING");
			}
			startService(mDropbearDaemonHandlerService);
			startLongRunningOperation();
		}
	}

	public void stopDropbear() {
		if (Base.debug) {
			Log.v(TAG, "stopDropbear() called. Current status = " + Base.getDropbearDaemonStatus() );
		}
		if ((Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STARTED) ||
				Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STARTING) {
			Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPING);
			int pid = Util.getDropbearPidFromPidFile(Base.getDropbearPidFilePath());
			if(Base.debug) {
				Log.d(TAG, "stopDropbear() killing pid " + pid);
				Log.d(TAG, "dropbearDaemonStatus = Base.DAEMON_STATUS_STOPPING");
			}
			String cmd = "kill -2 " + pid;
//			Util.doRun(cmd, Base.runDaemonAsRoot(), mLogviewHandler);
			Util.doRun(cmd, Base.runDaemonAsRoot(), null);
			stopService(mDropbearDaemonHandlerService);
		}
		startLongRunningOperation();
		Util.releaseWakeLock();
		Util.releaseWifiLock();
	}

	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
//		return super.onCreateOptionsMenu(menu);
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_settings:
			Intent p = new Intent(this, br.com.bott.droidsshd.activity.Preferences.class);
			startActivity(p);
			return true;
		case R.id.menu_quit:
			Util.showMsg("QUIT");
			this.finish();
			return true;
		case R.id.menu_refreshui:
			if(Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STARTING) { 
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTED);
			}
			if(Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STOPPING) {
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
			}
			updateStatus();
			return true;
		case R.id.menu_about:
//			Intent i = new Intent();
//			i.setAction("android.intent.action.VIEW");
//			i.setData("http://www.android.com");
//			i.setType(type)
			Util.showMsg("About");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	final Handler mHandler = new Handler();
	final Runnable mUpdateUI = new Runnable() {
		public void run() {
			updateStatus();
		}
	};

//	TODO - http://developer.android.com/resources/articles/painless-threading.html

	protected void startLongRunningOperation() {
		synchronized(this) {
			if (Base.debug) {
				Log.d(TAG, "startLongRunningOperation called");
			}
			
//			TODO - http://developer.android.com/resources/articles/timed-ui-updates.html
//			Runnable mUpdateTimeTask = new Runnable() {
//				public void run() {
//					final long start = mStartTime;
//					long millis = SystemClock.uptimeMillis() - start;
//					mHandler.postAtTime(this, start + (((minutes * 60) + seconds + 1) * 1000));
//			}
//			mHandler.removeCallbacks(mUpdateTimeTask);

			if(mMonitorDaemon!=null) {
				if(mMonitorDaemon.isAlive()) {
					mMonitorDaemon.extendLifetimeForAnother(System.currentTimeMillis()+2000);
				}
			} else {
				ReplicantThread mMonitorDaemon = new ReplicantThread(TAG, (System.currentTimeMillis()+2000), 600, mHandler, mUpdateUI, Base.debug);
				mMonitorDaemon.start();
			} 
		}
	}
	
/*	
 	final protected Handler mLogviewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg){
//			mStdOut.append(msg.getData().getString("line"));
//			mLogView.postDelayed(new Runnable() {
//				public void run() {
//					mLogView.fullScroll(ScrollView.FOCUS_DOWN);
//				}
//			}, 200); 
		}
	};
*/

	// DAEMON HANDLER
	private ServiceConnection mDaemonHandlerConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundDaemonHandlerService = ((br.com.bott.droidsshd.system.DroidSSHdService.DropbearDaemonHandlerBinder)service).getService();
			if(Base.debug) {
				Log.d(TAG, "onServiceConnected DroidSSHdService called");
				if (mBoundDaemonHandlerService==null){
					Log.d(TAG, "Failed to bind to DroidSSHdService (mBoundDaemonHandlerService is NULL)");
				} else {
					Log.d(TAG, "mBoundDaemonHandlerService = " + mBoundDaemonHandlerService.toString());
				}
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBoundDaemonHandlerService = null;
			if(Base.debug) {
				Log.d(TAG, "onServiceDisconnected called (mBoundDaemonHandlerService set to NULL)");
			}
		}
	};

	private void doBindDaemonHandlerService(Intent intent) {
		mDaemonHandlerIsBound = bindService(intent, mDaemonHandlerConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindDaemonHandlerService(Intent intent) {
		if (mDaemonHandlerIsBound) {
			unbindService(mDaemonHandlerConnection);
			mDaemonHandlerIsBound = false;
		}
	}

}


