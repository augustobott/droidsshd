package com.h3r3t1c.filechooser;


import br.com.bott.droidsshd.R;
//import br.com.bott.droidsshd.R.layout;

import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

//import android.app.Activity;
import android.content.Context;
//import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.view.View.OnClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.TextView;


public class FileArrayAdapter extends ArrayAdapter<Option>{

	private Context c;
	private int id;
	private List<Option>items;
	
	public FileArrayAdapter(Context context, int textViewResourceId,
			List<Option> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}
	public Option getItem(int i)
	{
		return items.get(i);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		final Option o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.file_chooser_upper);
			TextView t2 = (TextView) v.findViewById(R.id.file_chooser_lower);
			ImageView i = (ImageView) v.findViewById(R.id.file_chooser_icon);
			if(t1!=null)
				t1.setText(o.getName());
			if(t2!=null) {
				t2.setText(o.getData());
				if (o.getData().equalsIgnoreCase("folder")) {
					i.setImageResource(R.drawable.directory_icon);
//					i.setVisibility(View.VISIBLE);
				} else {
					if(o.getName().endsWith(".pub")){
						i.setImageResource(R.drawable.pubkey_icon);
					} else {
						i.setImageResource(R.drawable.file_icon);
					}
				}
			}
		}
		return v;
	}

}
