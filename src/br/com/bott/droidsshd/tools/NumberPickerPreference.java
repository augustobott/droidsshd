package br.com.bott.droidsshd.tools;

import com.quietlycoding.android.picker.NumberPicker;
import br.com.bott.droidsshd.R;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.View;

public class NumberPickerPreference extends DialogPreference {

//	private static final String TAG = "NumberPickerPreference";
//	private static final String PREF = "dropbear_port";
	protected NumberPicker picker;

	public NumberPickerPreference(Context context) {
		this(context, null);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.number_picker_pref);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		picker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
		picker.setRange(1, 65535);
		picker.setSpeed(50);
		picker.setCurrent(getValue());
	}

	public void onClick(DialogInterface dialog, int which) {
//		Log.d(TAG, "which: " + which);
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			picker.onClick(null);
			saveValue(picker.getCurrent());
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			break;
		default:
			break;	
		}
	}

	protected void saveValue(int val) {
		getEditor().putInt("dropbear_port", val).commit();
	}
	
	public int getValue() {
		// TODO - store that on preference/string/whatever 
		// TODO - (as long as it's the same as everything else)
		return getSharedPreferences().getInt("dropbear_port", 2222);
	}
}
