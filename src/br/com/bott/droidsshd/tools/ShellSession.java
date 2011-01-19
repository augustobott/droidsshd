package br.com.bott.droidsshd.tools;

//import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import br.com.bott.droidsshd.system.Base;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ShellSession extends Thread {

	protected final boolean debug;
	protected final boolean root;
	protected final String tag;
	protected final Handler handler;
	protected final Context context;
	protected String cmd;
	protected Process process;
	protected PrintStream stdin;
	protected SessionOutputThread stdout;
	protected SessionOutputThread stderr;

	public ShellSession(String tag, String cmd, Handler handler, boolean root, boolean debug) {
		super(tag + "-stdin");
		this.cmd=cmd;
		this.tag = tag;
		this.root = root;
		this.debug = debug;
		this.handler = handler;
		this.context = Base.getContext();
		if (debug) {
			Log.d(tag, String.format( 
					"new ShellSession(%s, %s, %B, %B)",
					tag, cmd, root, debug));
		}
	}
	
	public ShellSession(String tag, String cmd, boolean root, boolean debug) {
		super(tag + "-stdin");
		this.cmd=cmd;
		this.tag = tag;
		this.root = root;
		this.debug = debug;
		this.handler = null;
		this.context = Base.getContext();
		if (debug) {
			Log.d(tag, String.format( 
					"new ShellSession(%s, %s, %B, %B)",
					tag, cmd, root, debug));
		}
	}

	public ShellSession(String tag, boolean debug) {
		super(tag + "-stdin");
		this.cmd=null;
		this.tag = tag;
		this.root = false;
		this.debug = debug;
		this.handler = null;
		this.context = Base.getContext();
		if (debug) {
			Log.d(tag, String.format( 
					"new ShellSession(%s, %s, %B, %B)",
					tag, cmd, root, debug));
		}
	}

	public ShellSession(String tag, boolean root, boolean debug, Handler handler) {
		super(tag + "-stdin");
		this.cmd = null;
		this.tag = tag;
		this.root = root;
		this.debug = debug;
		this.handler = handler;
		this.context = Base.getContext();
		if (debug) {
			Log.d(tag, String.format( 
					"new ShellSession(%s, %s, %B, %B)",
					tag, cmd, root, debug));
		}
	}

	public final void run() {
		try {
			if (root) {
				if (debug) {
					Log.d(tag, "Starting new ROOT shell");
				}
				process = Runtime.getRuntime().exec(Base.SU_BIN);
//				process = Runtime.getRuntime().exec(Base.SU_BIN, null, new File("/sdcard"));
			} else {
				if (debug) {
					Log.d(tag, "Starting new shell");
				}
				process = Runtime.getRuntime().exec(Base.SH_BIN);
//				process = Runtime.getRuntime().exec(Base.SH_BIN, null, new File("/sdcard"));
			}
			
			stdin = new PrintStream(process.getOutputStream());

			stdout = new SessionOutputThread(tag + "-stdout", process.getInputStream(), debug) {
				@Override
				protected void onOutput(String line) {
					onStdOut(line);
				};
			};
			stdout.start();

			stderr = new SessionOutputThread(tag + "-stderr", process.getErrorStream(), debug) {
				@Override
				protected void onOutput(String line) {
					onStdErr(line);
				};
			};
			stderr.start();

			if (cmd!=null) {
   				cmd(cmd);
   				exit();
   			} else {
   				onSessionReady();
   			}
		} catch (IOException e) {
			Log.e(tag, "IOError: ", e);
		} finally {
			if (stdin != null) {
	   			if (root) {
					exit();
				}
				stdin.close();
			}
		}
		onSessionFinished();
	}

	public void cmd(String line) {
		if (debug) {
			Log.d(tag, line);
		}
		stdin.println(line);
		stdin.flush();
	}
	
	public void exit() {
		cmd("exit");
	}

	protected void onStdOut(String line) {
		// to be overridden
	}
	
	protected void onStdErr(String line) {
		// to be overridden
	}
	
	protected void onSessionReady() {
		// to be overridden
	}

	protected void onSessionFinished() {
		// to be overridden
	}

/*	public final void joinStdOutErr() throws InterruptedException {
		stdout.join();
		stderr.join();
	}
*/
	public final int waitFor() {
		int errno = Integer.MIN_VALUE;
		if (process != null) {
			waitFor: do {
				try {
					errno = process.waitFor();
				} catch (InterruptedException e) {
					continue waitFor;
				}
			} while (false);
		}
		return errno;
	}
}
