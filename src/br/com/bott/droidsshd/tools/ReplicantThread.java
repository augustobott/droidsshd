/**
 * 
 */
package br.com.bott.droidsshd.tools;

import android.os.Handler;
import android.util.Log;

/**
 * @author mestre
 *
 */
public class ReplicantThread extends Thread {

	protected String tag;
	protected long untilWhenMs;
	protected long checkEveryMs;
	protected long timeLeftMs;
	protected boolean notdone;
	protected boolean debug;
	protected Handler handler;
	protected Runnable updater;

	public ReplicantThread(String tag, long untilWhenMs, long checkEveryMs, Handler handler, Runnable updater, boolean debug) {
		this.debug=debug;
		this.notdone=true;
		this.untilWhenMs=untilWhenMs;
		this.checkEveryMs=checkEveryMs;
		this.handler=handler;
		this.updater=updater;
		this.tag=tag;
		this.timeLeftMs=Integer.MAX_VALUE;
		setName(tag);
		if (this.debug) {
			Log.v(this.tag, "new ReplicantThread created");
		}
	}
	
	public void setDone() {
				this.notdone=false;
	}
	
	public void extendLifetimeForAnother(long untilWhenMs) {
			this.untilWhenMs=untilWhenMs;
			this.notdone=true;
	}
	
	public void run() {
//		while(((timeLeftMs) > 0) && (notdone)){
		while(notdone){
			handler.post(updater);
			try {
				sleep(checkEveryMs);
				timeLeftMs=untilWhenMs-System.currentTimeMillis();
				if (timeLeftMs <= 0) {
					notdone=false;
				} else {
					if(debug) {
						Log.v(this.tag + "-DS-" + this.toString(), "Entered the loop " + this.toString());
						Log.v(this.tag + "-DS-" + this.toString(), "We'll stop running in at most " + timeLeftMs + " ms");
					}
				}
			} catch (InterruptedException e) {
					notdone=false;
				e.printStackTrace();
			}
		}
	}

}
