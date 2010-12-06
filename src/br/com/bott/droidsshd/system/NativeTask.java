package br.com.bott.droidsshd.system;

import android.util.Log;

public class NativeTask {
	public static final String TAG = "DroidSSHd NativeTask";

	static {
		try {
			Log.i(TAG, "Trying to load libNativeTask.so");
			System.loadLibrary("NativeTask");
		} catch (UnsatisfiedLinkError ule) {
			Log.e(TAG, "Could not load libNativeTask.so");
		}
	}
	public static native int runCommand(String command);
	public static native int chmod(String path, int mode);
	public static native int symlink(String origin, String destination);
}
