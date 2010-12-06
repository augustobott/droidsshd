/**
 * 
 */
package br.com.bott.droidsshd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import br.com.bott.droidsshd.system.Base;

import android.util.Log;

/**
 * @author mestre
 *
 */
public class SessionOutputThread extends Thread {

	protected final String tag;
	protected final InputStream is;
	protected final boolean debug;

	public SessionOutputThread(String tag, InputStream is, boolean debug){
		this.tag = tag;
		this.is = is;
		this.debug = debug;
		setName(tag);
	}
	
	@Override
	public void run() {
		try {
			if(Base.debug) {
				Log.i( tag, "Started");
			}
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is),512);
			String line;
			while(null!=(line=lnr.readLine())) {
				if (debug) {
					Log.d(tag, line);
				}
				onOutput(line);
			}
		} catch (Exception e) {
			Log.e(tag, "Error");
			e.printStackTrace();
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(tag, "IOException.");
					e.printStackTrace();
				}
			}
			if(Base.debug) {
				Log.i( tag, "Finished");
			}
			onFinish();
		}
	}

	protected void onOutput(String line) {
		// to be overridden
	}
	
	protected void onFinish() {
		// to be overridden
	}
}
