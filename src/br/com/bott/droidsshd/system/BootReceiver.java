/**
 * 
 */
package br.com.bott.droidsshd.system;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Receives a broadcast message when the device completes 
 * booting/starting up and checks if anything needs to be 
 * done. 
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
//				remove our stale PID file as root (just in case), no output handler (null)
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
	}
}

	   
