package dv606.rw222ci.rssreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Activity used edit preferences.<br><br>
 *
 * Changing the Automatic Update preference will directly
 * force an update to be made. The new preference will then be
 * applied. Currently, this is to, for example, avoid going from "disabled"
 * auto-update to "enabled" auto-update, and having to update
 * manually once before the settings takes action.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssReaderPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.rssreader_preference_header, target);
    }

    @Override
    protected boolean isValidFragment (String fragmentName) {
        return (RssReaderPreferenceFragment.class.getName().equals(fragmentName));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.auto_up_int_pref_key))) { // If the auto update preference was changed
            RssReaderPreferences.this.startService(
                    new Intent(RssReaderPreferences.this, RssFeedFetcherService.class));
        }
    }
}
