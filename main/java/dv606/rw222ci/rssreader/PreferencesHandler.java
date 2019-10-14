package dv606.rw222ci.rssreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Singleton class used to handle different preferences.<br>
 * Instead of accessing the <code>SharedPreferences</code> directly,
 * an instance of this method can be used to access them.<br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class PreferencesHandler {
    private static PreferencesHandler mInstance;

    private Context context;
    private SharedPreferences generalPrefs;
    private SharedPreferences widgetPrefs;

    /**
     * Get an PreferenceHandler instance.
     * @param context Just some context required to access SharedPreferences
     * @return A PreferenceHandler instance
     */
    public static PreferencesHandler getInstance(Context context){
        if(mInstance == null) {
            mInstance = new PreferencesHandler(context);
        }
        return mInstance;
    }

    /**
     * Creates a PreferenceHandler instance
     * @param context Just some context required to access SharedPreferences
     */
    private PreferencesHandler(Context context){
        generalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        widgetPrefs = context.getSharedPreferences(
                context.getString(R.string.widget_pref_key), Context.MODE_PRIVATE);
        this.context = context;
    }

    /**
     * Get the current automatic update interval in minutes.
     * @return The current automatic update interval.
     */
    public int getAutoUpdateInterval(){
        return Integer.valueOf(generalPrefs.getString(
                context.getString(R.string.auto_up_int_pref_key), "0"));
    }

    /**
     * Get whether a given widget has been configured or not.
     * @param id The id of widget to be checked.
     * @return True, if the widget has been configured; false otherwise.
     */
    public boolean isWidgetConfigured(int id){
       return widgetPrefs.getBoolean(
               id + context.getString(R.string.widget_isConf_key), false);
    }

    /**
     * Get the id of the feed a given widget is configured to display articles from.<br>
     * If no such preference has been added for the widget, -1 will be returned instead.
     * @param id The id of the widget.
     * @return The id of the feed the widget should display articles from.
     */
    public int getWidgetFeedId(int id){
        return widgetPrefs.getInt(
                id + context.getString(R.string.widget_feed_id_key), -1);
    }

    /**
     * Get the number of articles a given widget is configured to display.
     * @param id the id of the widget.
     * @return The number of articles the widget should display.
     */
    public int getWidgetArticleCount(int id){
        return widgetPrefs.getInt(
                id + context.getString(R.string.widget_count_key), 0);
    }

    /**
     * Add a new widget to the preferences.
     * @param widgetId The id of the widget.
     * @param feedId The id of the feed the widget should display articles from.
     * @param count The number of articles the widget should display.
     * @param isConfigured Whether or not the widget has been configured.
     * @return True, if the preferences were successfully stored; false otherwise.
     */
    public boolean addWidget(int widgetId, int feedId, int count, boolean isConfigured){
        SharedPreferences.Editor editor = widgetPrefs.edit();
        editor.putInt(
                widgetId + context.getString(R.string.widget_feed_id_key), feedId);
        editor.putInt(
                widgetId + context.getString(R.string.widget_count_key), count);
        editor.putBoolean(
                widgetId + context.getString(R.string.widget_isConf_key), isConfigured);
        return editor.commit();
    }

    /**
     * Removes a widget's preferences from the shared preferences.
     * @param id The widget which preferences should be removed.
     * @return True, if the preferences were successfully removed; false otherwise.
     */
    public boolean deleteWidget(int id){
        SharedPreferences.Editor editor = widgetPrefs.edit();
        editor.remove(
                id + context.getString(R.string.widget_feed_id_key));
        editor.remove(
                id + context.getString(R.string.widget_count_key));
        editor.remove(
                id + context.getString(R.string.widget_isConf_key));
        return editor.commit();
    }

    /**
     * Get the general preferences.
     * @return The general preferences.
     */
    public SharedPreferences getGeneralPreferences(){
        return generalPrefs;
    }

    /**
     * Get the widget preferences.
     * @return The widget preferences.
     */
    public SharedPreferences getWidgetPreferences(){
        return widgetPrefs;
    }
}
