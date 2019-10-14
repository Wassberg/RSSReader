package dv606.rw222ci.rssreader;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Preference fragment.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssReaderPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rssreader_preferences);
    }
}
