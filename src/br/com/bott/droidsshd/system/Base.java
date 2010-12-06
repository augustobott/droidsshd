/**
 * 
 */
package br.com.bott.droidsshd.system;


import br.com.bott.droidsshd.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author mestre
 *
 */
public class Base {

	/**
	 *  CONSTANTS
	 */
	
	public static final String TAG = "DroidSSHd-Base";
	
	public static final String THIS_PACKAGE_NAME = "br.com.bott.droidsshd";
	
	public static final String SU_BIN = "/system/xbin/su";
	public static final String SH_BIN = "/system/bin/sh";

	public static final String DROPBEAR_DIR_KEY = "etc";
	public static final String DROPBEAR_DIR_TMP = "run";
	public static final String DROPBEAR_DIR_BIN = "bin";
	public static final String DROPBEAR_PIDFILE = "dropbear.pid";
	public static final String DROPBEAR_DSS_HOST_KEY = "dropbear_dss_host_key";
	public static final String DROPBEAR_RSA_HOST_KEY = "dropbear_rsa_host_key";
	public static final String DROPBEAR_AUTHORIZED_KEYS = "authorized_keys";
	
	public static final String DROPBEAR_BIN_MUL = "dropbearmulti";
	public static final String DROPBEAR_BIN_SRV = "dropbear";
	public static final String DROPBEAR_BIN_KEY = "dropbearkey";
	public static final String DROPBEAR_BIN_SCP = "scp";
	
	
	public static final int DAEMON_STATUS_UNKNOWN = -1;
	public static final int DAEMON_STATUS_STOPPED = 0;
	public static final int DAEMON_STATUS_STARTED = 1;
	public static final int DAEMON_STATUS_STARTING = 2;
	public static final int DAEMON_STATUS_STOPPING = 3;

//	public static final int WIFI_STATUS_UNKNOWN = -1;
//	public static final int WIFI_STATUS_DISABLED = 0;
//	public static final int WIFI_STATUS_ENABLED = 1;

	/**
	 *  GLOBAL STUFF
	 */

	public static boolean debug;
	protected static int daemonPort;
	protected static boolean runDaemonAsRoot;
	protected static boolean startedAsRoot;
	protected static boolean startAtBoot;
	protected static boolean startAtBootOnlyIfRunningBefore;
	protected static boolean notificationsEnabled;
	protected static boolean wakeLock;
	protected static boolean wifiLock;

	protected static Context context = null;
	protected static String username = "android";
	protected static String password = "password";
//	protected static File homeDir = null;

//	private boolean mRestartRequired;
	protected static String filesDirPath;
	protected static String dropbearEtcDir;
	protected static String dropbearTmpDir;
	protected static String dropbearBinDir;
	protected static String dropbearPidFile;
	
	protected static int dropbearDaemonStatus = DAEMON_STATUS_UNKNOWN;


	public static boolean startDaemonAtBoot() {
		return startAtBoot; 
	}

	public static boolean startDaemonAtBootOnlyIfRunningBefore() {
		return startAtBootOnlyIfRunningBefore; 
	}

	public static boolean runDaemonAsRoot() {
		return runDaemonAsRoot;
	}

	public static void setDaemonStartedAsRoot(boolean b) {
		startedAsRoot = b;
	}
	
	public static boolean isDropbearDaemonNotificationEnabled() {
		return notificationsEnabled;
	}

	public static int getDropbearDaemonStatus(){
		return dropbearDaemonStatus;
	}
	
	public static void setDropbearDaemonStatus(int s){
		dropbearDaemonStatus=s;
//		this should be on a service...
//		if (s==Base.DAEMON_STATUS_STOPPED) {
//			stopSelf();
//		}
	}
	
	public static boolean shouldAcquireWakeLock(){
		return wakeLock;
	}

	public static boolean shouldAcquireWifiLock(){
		return wifiLock;
	}

	public static String getFilesDirPath(){
		return filesDirPath;
	}

	private static void setDropbearPidFilePath(String path) {
		Base.dropbearPidFile=path;
		if (debug) {
			Log.d(TAG, "Base.dropbearPidFile = " + path );
		}
	}

	public static String getDropbearPidFilePath(){
		return dropbearPidFile;
	}
	
	public static void setDaemonPort(int port) {
		Base.daemonPort=port;
		if (debug) {
			Log.d(TAG, "Base.setDaemonPort = " + port );
		}
	}
	
	public static int getDaemonPort() {
		return daemonPort;
	}
	
	public static void setDropbearEtcDirPath(String path) {
		Base.dropbearEtcDir=path;
		if (debug) {
			Log.d(TAG, "Base.setDropbearEtcDir = " + path );
		}
	}

	public static String getDropbearEtcDirPath() {
		return dropbearEtcDir;
	}

	public static void setDropbearTmpDirPath(String path) {
		Base.dropbearTmpDir=path;
		if (debug) {
			Log.d(TAG, "Base.setDropbearTmpDir = " + path );
		}
	}

	public static String getDropbearTmpDirPath() {
		return dropbearTmpDir;
	}

	public static void setDropbearBinDirPath(String path) {
		Base.dropbearBinDir=path;
		if (debug) {
			Log.d(TAG, "Base.setDropbearBinDir = " + path );
		}
	}

	public static String getDropbearBinDirPath() {
		return dropbearBinDir;
	}

	public static String getDropbearDssHostKeyFilePath(){
		return (dropbearEtcDir + "/" + DROPBEAR_DSS_HOST_KEY);
	}

	public static String getDropbearRsaHostKeyFilePath(){
		return (dropbearEtcDir + "/" + DROPBEAR_RSA_HOST_KEY);
	}

	public static String getDropbearAuthorizedKeysFilePath(){
		return (dropbearEtcDir + "/" + DROPBEAR_AUTHORIZED_KEYS);
	}

/*	public static File getHomeDir() {
		return homeDir;
	}
*/

/*	public static boolean setHomeDir(File homeDir) {
		if(homeDir.isDirectory()) {
			Base.homeDir = homeDir;
			if (debug) {
				Log.d(TAG, "Base.setHomeDir = " + homeDir);
			}
			return true;
		}
		if (debug) {
			Log.d(TAG, "Base.setHomeDir called but " + homeDir + " is not a directory");
		}
		return false;
	}
*/

	public static Context getContext() {
		return Base.context;
	}
	
	public static void setContext(Context context) {
		if (context != null) {
			Base.context = context;
			if (debug) {
				Log.d(TAG, "Base.context = " + context.toString());
			}
			refresh();
		} else {
			if (debug) {
				Log.d(TAG, "Base.context called but context is null (so not set)");
			}
		}
	}
	
	public static String getUsername() {
		return username;
	}
	
	public static void setUsername(String username) {
		Base.username=username;
	}
	
	public static String getPassword() {
		return password;
	}
	
	public static void refresh() {
		if (Base.context != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Base.getContext());
			debug = sp.getBoolean(context.getString(R.string.pref_debug_key), false);
//			daemonPort = Integer.parseInt(sp.getString(context.getString(R.string.pref_dropbear_port_key), "50022"));
			daemonPort = sp.getInt(context.getString(R.string.pref_dropbear_port_key), 2222);
			startAtBoot = sp.getBoolean(context.getString(R.string.pref_dropbear_start_on_boot_key), false);
			startAtBootOnlyIfRunningBefore = sp.getBoolean(context.getString(R.string.pref_dropbear_start_on_boot_only_if_was_running_key), true);
			notificationsEnabled = sp.getBoolean(context.getString(R.string.pref_interface_notification_key), true);
			password = sp.getString(context.getString(R.string.pref_dropbear_auth_password_key), "password");
			runDaemonAsRoot = sp.getBoolean(context.getString(R.string.pref_dropbear_as_root_key),false);
//			username = sp.getString(context.getString(R.string.pref_dropbear_auth_username_key), "android");
			if(runDaemonAsRoot) {
				username="root";
			} else {
				username="android";
			}
			wakeLock = sp.getBoolean(context.getString(R.string.pref_prevent_device_sleep_key), false);
			wifiLock = sp.getBoolean(context.getString(R.string.pref_wifi_lock_key), false);
		}
	}
			
	public static void initialize(Context context) {
		if (context != null) {
			Base.context = context;
			Base.filesDirPath = Base.context.getFilesDir().getAbsolutePath();
			setDropbearEtcDirPath(Base.filesDirPath +  "/" + Base.DROPBEAR_DIR_KEY);
			setDropbearTmpDirPath(Base.filesDirPath +  "/" + Base.DROPBEAR_DIR_TMP);
			setDropbearBinDirPath(Base.filesDirPath +  "/" + Base.DROPBEAR_DIR_BIN);
			setDropbearPidFilePath(Base.dropbearTmpDir + "/" + Base.DROPBEAR_PIDFILE);
		}
		refresh();
	}
}
