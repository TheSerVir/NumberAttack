package com.virstyuk.zapominaika;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public final class CountSeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {

    // Namespaces to read attributes
    private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.virstyuk.zapominaika";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    // Default values for defaults
    private static final int DEFAULT_CURRENT_VALUE = 3;
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 20;

    // Real defaults
    private final int mDefaultValue;
    private int mMaxValue;
    private final int mMinValue;
    
    // Current value
    private int mCurrentValue;
    
    // View elements
    private SeekBar mSeekBar;
    private TextView mValueText;
    
    private Context cont;

    public CountSeekBarPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	int size = Integer.parseInt(settings.getString("saved_size", "4"));
	
	// Read parameters from attributes
	mMinValue = 3;
	mMaxValue = size*size;
	mDefaultValue = size;
	cont = context;
    }

    @Override
    protected View onCreateDialogView() {
	// Get current value from preferences
	mCurrentValue = Integer.parseInt(getPersistedString(Integer.toString(mDefaultValue)));

	// Inflate layout
	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View view = inflater.inflate(R.layout.seek_bar, null);

	// Setup minimum and maximum text labels
	((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(mMinValue));
	((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(mMaxValue));

	// Setup SeekBar
	mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
	mSeekBar.setMax(mMaxValue - mMinValue);
	mSeekBar.setProgress(mCurrentValue - mMinValue);
	mSeekBar.setOnSeekBarChangeListener(this);

	// Setup text label for current value
	mValueText = (TextView) view.findViewById(R.id.current_value);
	mValueText.setText(Integer.toString(mCurrentValue));

	return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);

	// Return if change was cancelled
	if (!positiveResult) {
	    return;
	}
	
	// Persist current value if needed
	if (shouldPersist()) {
	    persistString(Integer.toString(mCurrentValue));
	}

	// Notify activity about changes (to update preference summary line)
	notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
	// Format summary string with current value
	String summary = super.getSummary().toString();
	int value = Integer.parseInt(getPersistedString(Integer.toString(mDefaultValue)));
	return String.format(summary, value);
    }
    
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
	// Update current value
	mCurrentValue = value + mMinValue;
	// Update label with current value
	mValueText.setText(Integer.toString(mCurrentValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
	// Not used
    }

    public void onStopTrackingTouch(SeekBar seek) {
	// Not used
    }
    
    public void setMax(int max) {
    	mMaxValue = max*max;
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(cont);
    	Editor ed = settings.edit();
        ed.putString("saved_count", Integer.toString(max));
        ed.commit();
    }
}