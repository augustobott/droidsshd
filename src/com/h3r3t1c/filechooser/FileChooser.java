/**
 * http://www.dreamincode.net/forums/topic/190013-creating-simple-file-chooser/
 */
package com.h3r3t1c.filechooser;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import br.com.bott.droidsshd.R;
import br.com.bott.droidsshd.system.Base;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
//import android.widget.Toast;

public class FileChooser extends ListActivity {

	private static final String TAG = "FileChooser";

	private File currentDir;
	private FileArrayAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		fill(currentDir);
	}

	private void fill(File f)
	{
		File[]dirs = f.listFiles();
		this.setTitle("Current Dir: "+f.getName());
		List<Option>dir = new ArrayList<Option>();
		List<Option>fls = new ArrayList<Option>();
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		}catch(Exception e)
		{
			
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if(!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0,new Option("..","Parent Directory",f.getParent()));
		adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
		this.setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		}
		else
		{
			onFileClick(o);
		}
	}
	private void onFileClick(Option o)
	{
//		Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
		Intent i = new Intent();
        i.putExtra("path", o.getPath());
        i.putExtra("name", o.getName());
        setResult(android.app.Activity.RESULT_OK, i);
        this.finish();
//        finishActivity(R.string.activity_file_chooser);
	}

	@Override
	public void onBackPressed() {
		if (Base.debug) {
			Log.v(TAG, "'" + currentDir.getParent() + "'");
		}
		if (currentDir.getParent().equalsIgnoreCase("/")) {
			setResult(RESULT_CANCELED);
			finish();
		} else {
			currentDir=currentDir.getParentFile();
			fill(currentDir);
		}
//		super.onBackPressed();
	}
}
