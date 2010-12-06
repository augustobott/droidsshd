/**
 * 
 */
package br.com.bott.droidsshd.system;

//import br.com.bott.droidsshd.lixo.MonitoringService;
//import br.com.bott.droidsshd.R;
import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import android.util.Log;

/*
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
*/

/**
 * Receives a broadcast message when the device completes 
 * booting/starting up and checks if anything needs to be 
 * done. Also used to manually start/stop monitoring services 
 * and alarms.  
 *
 * @author mestre
 *
 */
public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "DroidSSHdBootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Base.initialize(context);
		if (intent.getAction().equals(android.content.Intent.ACTION_BOOT_COMPLETED)) {
			File pid = new File(Base.getDropbearPidFilePath());
			boolean wasRunningBefore = pid.exists();
			boolean startDaemonAtBoot = Base.startDaemonAtBoot();
			boolean startDaemonAtBootOnlyIfRunningBefore = Base.startDaemonAtBootOnlyIfRunningBefore();

			if (Base.debug) {
				Log.d(TAG, "Received BOOT_COMPLETED broadcast");
				Log.d(TAG, "startDaemonAtBoot = " + startDaemonAtBoot);
				Log.d(TAG, "startDaemonAtBootOnlyIfRunningBefore = " + startDaemonAtBootOnlyIfRunningBefore);
				Log.d(TAG, "wasRunningBefore = " + wasRunningBefore);
			}

			if (wasRunningBefore) {
				if (Base.debug) {
					Log.d(TAG, "Removing stale PID file");
				}
//				Util.doRun("rm " + Base.getDropbearPidFilePath(), Base.runDaemonAsRoot(), null);
				Util.doRun("rm " + Base.getDropbearPidFilePath(), true, null);
			}

			if (startDaemonAtBoot) {
				if (Base.debug) {
					Log.d(TAG, "dropbear daemon configured to START on boot");
				}
				if (!startDaemonAtBootOnlyIfRunningBefore || (startDaemonAtBootOnlyIfRunningBefore && wasRunningBefore)) {
					Log.i(TAG, "Starding Dropbear daemon");
						context.startService(new Intent(context, br.com.bott.droidsshd.system.DroidSSHdService.class));
				}
			} else {
				if (Base.debug) {
					Log.d(TAG, "dropbear daemon configured to NOT start on boot");
				}
			}
		}
//			startAlarm(context);
	}

/*	private static AlarmManager mgr;
	private static PendingIntent pi = null;

	

	public static void cancelAlarm(){
			if (mgr != null) {
					mgr.cancel(pi);
			}
	}
	
	public static void startAlarm(Context context){
			AlarmSettings settings = Preferences.ReadAlarmSettings(PreferenceManager.getDefaultSharedPreferences(context));
			
			if (settings.isAlarmEnabled()) {
					// Set up PendingIntent for the alarm service
					mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
					Intent i = new Intent(context, AlarmReceiver.class);
					pi = PendingIntent.getBroadcast(context, 0, i, 0);
					// First intent after a small (2 second) delay and repeat at the user-set intervals
					mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000, settings.getAlarmIntervalInMilliseconds(), pi);
			}
	}
*/
}

	   
