/**
 * 
 */
package br.com.bott.droidsshd.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
//import br.com.bott.droidsshd.WifiLock;
import br.com.bott.droidsshd.tools.ShellSession;

/**
 * @author mestre
 *
 */
public class Util {
	private final static String TAG = "DroidSShd Util";

	private static PowerManager.WakeLock wakeLock;
	private static WifiLock wifiLock = null;


	public static void showMsg(String txt) {
		Toast toast = Toast.makeText(Base.getContext(), (CharSequence)txt, Toast.LENGTH_SHORT);
		toast.show();
		if(Base.debug) {
			Log.d(TAG, "showMsg() txt='"+txt+"'");
		}
	}

	public static boolean checkPathToBinaries(){
		if( ! new File(Base.getDropbearBinDirPath()+"/dropbear").exists() ) {
			Util.showMsg("dropbear not installed");
			Log.e(TAG, "Dropbear not installed");
			return false;
		}
		if (Base.debug) {
			Log.v(TAG, "dropbear binaries found");
		}
		return true;
	}
	
	public static int getDropbearPidFromPidFile(String path) {
		File pidFile = new File(path);
		int pid=0;
		if( ! pidFile.exists() ) {
			if(Base.debug) {
				Log.d(TAG, "Dropbear PID file doesn't exist:" + pidFile.getAbsolutePath());
				Log.d(TAG, "dropbearDaemonStatus = " + Base.getDropbearDaemonStatus());
			}
			// TODO - begin of ugly hack to fix a nasty bug
			if (Base.getDropbearDaemonStatus()==Base.DAEMON_STATUS_STOPPING) {
				if(Base.debug) {
					Log.v(TAG,"mCurrentStatus was STOPPING, changed to STOPPED");
				}
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
//				updateStatus();
			}
			// TODO - end of ugly hack - state changes can be tricky :-(
			return 0;
		} else {
			try {
				BufferedReader in = new BufferedReader(new FileReader(pidFile.getAbsolutePath()), 512);
				String line;
				String tmp="";
				while ((line = in.readLine()) != null) {
					if (Base.debug){
						Log.d(TAG, "getDropbearPid() line = " + line);
					}
					tmp += line; // + "\n";
				}
				in.close();
				if (Base.debug){
					Log.d(TAG, "getDropbearPid() tmp = " + tmp);
				}
				pid = Integer.parseInt(tmp);
				if (Base.debug){
					Log.d(TAG, "pidFile = " + pid + " - " + pidFile.getAbsolutePath() + " = " + tmp);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				if (Base.debug) {
					Log.d(TAG, "Can't read Dropbear PID file " + pidFile.getAbsolutePath());
				}
				return pid;
			}
		}
		return pid;
	}
	
//	public static boolean isDropbearDaemonRunning(boolean removeStalePidFile) {
	public static boolean isDropbearDaemonRunning() {
//		boolean removeStalePidFile = true;
		int pid;
		pid = Util.getDropbearPidFromPidFile(Base.getDropbearPidFilePath());
//		pid = getDropbearPidFromProcessList();
		if (pid!=0) {
			if(checkDaemonAgainstProcessId(pid)) {
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STARTED);
				return true;
			} else {
				if (Base.debug) {
					Log.d(TAG, "isDropbearDaemonRunning: we have a stale pidfile");
//					Log.d(TAG, "removeStalePidFile = " + removeStalePidFile);
				}
//				if(removeStalePidFile) {
//					doRun("rm " + Base.getDropbearPidFilePath(), Base.asRoot, mLogviewHandler);
					doRun("rm " + Base.getDropbearPidFilePath(), Base.runDaemonAsRoot(), null);
//				}
				Base.setDropbearDaemonStatus(Base.DAEMON_STATUS_STOPPED);
				return false;
			}
		}
		return false;
	}

	protected static boolean checkDaemonAgainstProcessId(int pid) {
		// TODO - check if this PID is actually dropbear's
		File mProc = new File("/proc/" + pid);
		return mProc.isDirectory();
	}

	public static void doRun(String cmd, boolean asRoot, Handler handler){
		ShellSession p = new ShellSession(TAG + "-shell", cmd, handler, asRoot, Base.debug) {
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
		p.start();
	}

	public static boolean validateHostKeys() {
		return ((checkHostKey(Base.DROPBEAR_DSS_HOST_KEY)) && 
				 (checkHostKey(Base.DROPBEAR_RSA_HOST_KEY)));
	}

	public static boolean checkHostKey(String whichOne){
		File hostKey = new File(Base.getDropbearEtcDirPath() + "/" + whichOne);
		if (hostKey.exists()) {
			if(Base.debug) {
				Log.v(TAG, "Host key found: " + hostKey.getAbsolutePath());
			}
		} else {
			if(Base.debug) {
				Log.v(TAG, "Host key NOT found: " + hostKey.getAbsolutePath());
			}
			return false;
		}
		return true;
	}
	
	public static int chmod(String path, int mode){
		int out = NativeTask.chmod(path, mode);
		if(Base.debug) {
			Log.d(TAG, "chmod " + mode + " " + path + " returned " + out);
		}
		return out;
	}
	
	public static void copyFile(String src, String dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
			byte[] buffer = new byte[1024];
			int len;
			while((len=in.read(buffer))>0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		}
	}
	
	public static boolean mkdir(String path){
		boolean out=false;
		File f = new File(path);
		if (!f.isDirectory()) {
			out=f.mkdirs();
		}
		if(Base.debug) {
			Log.d(TAG, "mkdir " + path + " returned " + out);
		}
		return out;
	}
		
	public static int symlink(String orig, String dest){
		int out = NativeTask.symlink(orig, dest);
		if(Base.debug) {
			Log.d(TAG, "symlink " + orig + " to " + dest + " returned " + out);
		}
		return out;
	}
	
	/*protected int getDropbearPidFromProcessList() {
	// TODO
	if (mDebug) {
		Log.d(TAG, "getDropbearPidFromProcessList() called");
	}

	String pid;
	String temp;
	pid = "";
	int i;
	try{
		Process p = Runtime.getRuntime().exec("ps");
		p.waitFor();
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ( (temp = stdInput.readLine()) != null ) {
			if (mDebug) {
				Log.d(TAG, "getDropbearPidFromProcessList temp='"+temp+"'");
			}
			if ( temp.contains(DROPBEAR_BIN) ) {
				if (mDebug) {
					Log.d(TAG, "getDropbearPidFromProcessList temp='"+temp+"'");
				}
				String [] cmdArray = temp.split(" +");
				for (i=0; i< cmdArray.length; i++) {
					if (mDebug) {
						Log.d(TAG, "loop i="+ i + " => " + cmdArray[i]);
					}
				}
				pid = cmdArray[1];
			}
		}
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	catch (InterruptedException e) {
		e.printStackTrace();
	}

	if ( pid != "") {
		i = Integer.parseInt(pid);
		if (mDebug) { 
			Log.d(TAG, "getDropbearPidFromProcessList pid='"+pid+"'");
		}
	} else {
		if (mDebug) { 
			Log.d(Base.debuggetDropbearPidFromProcessList pid='empty'");
		}
	}

	
	
	return 0;
}*/
	public static void mysleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Log.e(TAG, "mysleep(" + ms + ") had an InterruptedException");
			e.printStackTrace();
		}
	}

	synchronized public static void takeWakeLock() {
		if(wakeLock == null) {
			PowerManager pm = (PowerManager)Base.getContext().getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			wakeLock.setReferenceCounted(false);
			if (Base.debug) {
				Log.d( TAG, "Acquiring WAKE lock");
			}
			wakeLock.acquire();
		}
	}

	synchronized public static void releaseWakeLock() {
		if(wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
			if (Base.debug) {
				Log.d(TAG, "Releasing WAKE lock");
			}
		} /*else {
			if (Base.debug) {
				Log.i(TAG, "Couldn't release null wake lock");
			}
		}*/
	}

//	http://code.google.com/p/swiftp/source/browse/#svn/trunk/src/org/swiftp

	synchronized public static void takeWifiLock() {
		if(wifiLock == null) {
			WifiManager manager = (WifiManager)Base.getContext().getSystemService(Context.WIFI_SERVICE);
			wifiLock = manager.createWifiLock("DroidSSHd");
			wifiLock.setReferenceCounted(false);
		}
		wifiLock.acquire();
		if (Base.debug) {
			Log.d(TAG, "Acquiring WIFI lock");
		}
	}
	
	synchronized public static void releaseWifiLock() {
		if(wifiLock != null) {
			wifiLock.release();
			wifiLock = null;
			if (Base.debug) {
				Log.d(TAG, "Releasing WIFI lock");
			}
		}
	}

	synchronized public static boolean isWifiEnabled() {
		WifiManager wifiMgr = (WifiManager)Base.getContext().getSystemService(Context.WIFI_SERVICE);
		if(wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		} else {
			return false;
		}
	}

	public static Iterator<String> getLocalIpAddress() {
		ArrayList<String> ipAddr = new ArrayList<String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (Base.debug) {
							Log.v(TAG, "Interface " + intf.getDisplayName() + ", IPaddress " + inetAddress.getHostAddress().toString() );
						}
						ipAddr.add(inetAddress.getHostAddress().toString());
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
			return null;
		}
		return ipAddr.iterator();
	}

}
