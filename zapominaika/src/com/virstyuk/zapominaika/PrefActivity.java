package com.virstyuk.zapominaika;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PrefActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
     
    Preference rateit = (Preference) findPreference("rate_button");
    rateit.setOnPreferenceClickListener(new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			final String appPackageName = getPackageName();
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
			return false;
		}
    	
    });
    /*			final String appPackageName = getPackageName();
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}*/
    Preference.OnPreferenceChangeListener spChanged = new
        Preference.OnPreferenceChangeListener() {
	    	
	    	int size;
	    	
	    	@Override
	    	public boolean onPreferenceChange(Preference preference,
					Object newValue) {
	    		CountSeekBarPreference countPref = (CountSeekBarPreference) findPreference("saved_count");
	    		countPref.setMax(Integer.parseInt((String) newValue));
	    		return true;
			}
		};

	Preference sizePref = (Preference) findPreference("saved_size");
	sizePref.setOnPreferenceChangeListener(spChanged);
  }

  
}