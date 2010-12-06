/**
 * 
 */
package br.com.bott.droidsshd.system;

import br.com.bott.droidsshd.R;
import br.com.bott.droidsshd.tools.ShellSession;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * @author mestre
 *
 */
public class DroidSSHdService extends Service{ 

	private static final String TAG = "DroidSSHdService";

	private static NotificationManager mNotificationManager;
	private static FileObserver mPidWatchdog;

	private static int dropbearDaemonProcessId = 0;
	private static boolean dropbearDaemonRunning = false;

	private static boolean dropbearDaemonNotificationShown = false;

	private Handler serviceHandler = new Handler();
	
	// lock to handle (synchronized) FileObserver calls
	private static Object sLock = new Object();

	public boolean isDaemonRunning() {
		return dropbearDaemonRunning;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO - implement further checks on Base.init for special cases 
		// TODO - i.e. this SVC was started on boot and DroidSSHd activity hasn't run just yet
		// TODO - (is it the same ctx? is it null? etc...) 
		Base.initialize(getBaseContext());
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		if (Base.debug) {
			Log.d(TAG, "onCreate called");
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (Base.debug) {
			Log.d(TAG, "onStart(" + intent.toString() + ", " + startId + ") called");
		}
		handleStart(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Base.debug) {
			Log.d(TAG, "onStart(" + intent.toString() + ", " + flags + ", " + startId + ") called");
		}
		handleStart(intent, flags, startId);
		return Service.START_STICKY;
	}
	
	private void handleStart(Intent intent, int flags, int startId) {
		setUpPidFileWatchdog(Base.getDropbearTmpDirPath());
		if (serviceHandler!=null) {
			serviceHandler.post(new updateDaemonStatus());
			if (!dropbearDaemonRunning) {
				serviceHandler.postDelayed(new startDropbear(), 200L);
				if (Base.debug) {
					Log.d(TAG, "handleStart called, starting dropbear daemon");
				}
			} else {
				if (Base.debug) {
					Log.d(TAG, "handleStart called, but dropbear is already running");
				}
			}
		} else {
			Log.e(TAG, "handleStart called, but serviceHandler is NULL!");
			handleStop();
		}
	}
	
	private void handleStop() {
		if (mPidWatchdog!=null) {
			mPidWatchdog.stopWatching();
			mPidWatchdog = null;
		}
		Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
		stopSelf();
	}

	@Override
	public void onDestroy() {
		handleStop();
		super.onDestroy();
	}
	
	protected class startDropbear implements Runnable {
		public void run(){
			if(Base.debug) {
				Log.d(TAG+"-startDropbear", "starting");
			}
			if (!dropbearDaemonRunning) {
	//			ShellSession mDaemonSession = new ShellSession(TAG+"-daemon", Base.runDaemonAsRoot(), Base.debug, mLogviewHandler) {
				ShellSession mDaemonSession = new ShellSession(TAG+"-daemon", Base.runDaemonAsRoot(), Base.debug, null) {
					@Override 
					protected void onSessionReady() {
						if (debug) {
							Log.v(tag, "onSessionReady called");
						}
						int uid;
						if (Base.runDaemonAsRoot()) {
							uid = 0;
						} else {
							uid = android.os.Process.myUid();
						}
						cmd = String.format("%s -d %s -r %s -p %d -P %s -E -R %s -A -N %s -U %s -G %s -C %s",
	//							-F
								Base.getDropbearBinDirPath() + "/" + Base.DROPBEAR_BIN_SRV,
								Base.getDropbearDssHostKeyFilePath(),
								Base.getDropbearRsaHostKeyFilePath(),
								Base.getDaemonPort(),
								Base.getDropbearPidFilePath(),
								Base.getDropbearAuthorizedKeysFilePath(),
								Base.getUsername(),
								uid,
								uid,
								Base.getPassword()
								);
						if (debug) {
							Log.v(tag, "cmd = '" + cmd + "'");
						}
	//					cmd("export HOME=/mnt/sdcard");
	//					cmd("cd $HOME");
						cmd(cmd);
					}
					@Override
					protected void onStdOut(String line) {
						if(debug) {
							Log.d(tag, "'" + line + "'");
						}
						if (handler!=null) {
							Message message = new Message();
							Bundle data = new Bundle();
							data.putCharSequence("line", line + "\n");
							message.setData(data);
							handler.sendMessage(message);
						}
					}
					@Override
					protected void onStdErr(String line) { 
						onStdOut (line);
					}
				};
				mDaemonSession.start();
			} else {
				Log.i(TAG, "dropbear daemon already running");
			}
		}
	}

	private FileObserver createPidWatchdog(String path,int mask) {
		FileObserver observer = new FileObserver(path, mask) {
			@Override
			public void onEvent(int event, String path) {
				synchronized(sLock) {
					switch (event) {
					case FileObserver.CREATE:
						Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTING);
						if(Base.debug) {
							Log.v(TAG, "File " + path + " created");
							Log.d(TAG, "mCurrentStatus = Base.DAEMON_STATUS_STARTING");
						}
						break;
					case FileObserver.DELETE:
//						if ((Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STARTED) 
//								|| (Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STARTING)
//								|| (Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STOPPING)) {
							Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
//							mPidWatchdog.stopWatching();
//							mPidWatchdog = null;
//							stopSelf();
//						}
						hideNotification();
						if (Base.debug){
							Log.v(TAG, "File " + path + " deleted");
							Log.d(TAG, "mCurrentStatus = Base.DAEMON_STATUS_STOPPED");
						}
						break;
					case FileObserver.MODIFY:
						Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTED);
						showNotification();
						if (Base.debug){
							Log.v(TAG, "File " + path + " modified");
							Log.d(TAG, "mCurrentStatus = Base.DAEMON_STATUS_STARTED");
						}
						break;
					default:
						if (Base.debug){
							Log.v(TAG, "event = " + event + " path = " + path);
						}
						break;
					}
				}
			}
		};
		return observer;
	}

	private void setUpPidFileWatchdog(String path) {
		int mask = FileObserver.CREATE + FileObserver.DELETE + FileObserver.MODIFY;
		if (mPidWatchdog!=null) {
			if (Base.debug) {
				Log.d(TAG, "setUpPidFileWatchdog called but PIDObserver has already been set-up");
			}
		} else {
			mPidWatchdog = createPidWatchdog(path, mask);
			mPidWatchdog.startWatching();
		}
		if (Base.debug) {
			Log.d(TAG, "PIDObserver.toString() = " + mPidWatchdog.toString());
			Log.d(TAG, "Watching " + path + " mask " + mask);
		}
	}

	protected void showNotification() {
		if (Base.isDropbearDaemonNotificationEnabled()) {
			Notification notifyDetails = new Notification(R.drawable.ssh_icon, getString(R.string.app_label), System.currentTimeMillis());
			Intent intent = new Intent();
			intent.setClass(Base.getContext(), br.com.bott.droidsshd.DroidSSHd.class);
			intent.setAction(Intent.ACTION_DEFAULT);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			notifyDetails.setLatestEventInfo(this, getString(R.string.app_label), "Dropbear listening on port " + Base.getDaemonPort(), pendingIntent);
			notifyDetails.flags |= Notification.FLAG_ONGOING_EVENT;
			notifyDetails.flags |= Notification.DEFAULT_SOUND;
			mNotificationManager.notify(R.string.app_label, notifyDetails);
			dropbearDaemonNotificationShown=true;
			if (Base.shouldAcquireWakeLock()) {
				Util.takeWakeLock();
			}
			if ((Base.shouldAcquireWifiLock()) && (Util.isWifiEnabled())) {
				Util.takeWifiLock();
			}
		}
	}

	protected void hideNotification() {
		if (dropbearDaemonNotificationShown) {
			mNotificationManager.cancel(R.string.app_label);
			dropbearDaemonNotificationShown=false;
			Util.releaseWakeLock();
			Util.releaseWifiLock();
		}
	}
	
	protected class updateDaemonStatus implements Runnable {
		public void run(){
			if(Base.debug) {
				Log.d(TAG+"-updateDaemonStatus", "started");
			}
			dropbearDaemonProcessId = Util.getDropbearPidFromPidFile(Base.getDropbearPidFilePath());
			if (dropbearDaemonProcessId!=0) {
				dropbearDaemonRunning = Util.isDropbearDaemonRunning();
			} else {
				dropbearDaemonRunning = false;
			}
			if(dropbearDaemonRunning) {
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTED);
				showNotification();
			} else {
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
				hideNotification();
			}
		}
	}

	private final IBinder mBinder = new DropbearDaemonHandlerBinder();
	
	public class DropbearDaemonHandlerBinder extends Binder {
		public DroidSSHdService getService() {
			return DroidSSHdService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
