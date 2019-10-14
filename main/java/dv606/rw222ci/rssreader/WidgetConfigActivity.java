package dv606.rw222ci.rssreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The activity for configurations of widgets.<br><br>
 *
 * The user is given the option to choose which feed the widget
 * should display recent articles from. The user can also chose to
 * let the widget display the recent articles across all feeds.<br>
 * Once choosing one of the alternatives, the user also get the option
 * to choose how many feeds that should be displayed.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class WidgetConfigActivity extends Activity{
    int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private RssDBHandler rssDB;
    private SharedPreferences.Editor editor;
    public static final int ANY_FEED_BUTTON_ID = -1;

    private int chosenId;
    private int chosenCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, new Intent());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            rssDB = RssDBHandler.getInstance(this); // Access database

            setContentView(R.layout.rsswidget_conf_layout);
            ArrayList<RssFeed> feeds = rssDB.getAllFeeds();
            if(feeds.size() <= 0) { // No feeds available
                TextView noFeedsView = (TextView) findViewById(R.id.widget_no_feeds_view);
                noFeedsView.setVisibility(View.VISIBLE);
            }
            else if(feeds.size() > 1) { // More than one feed available
                Button anyFeedsButton = (Button) findViewById(R.id.any_feed_button);
                anyFeedsButton.setVisibility(View.VISIBLE); // Allow "Any feed" to be chosen
                anyFeedsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chosenId = ANY_FEED_BUTTON_ID;
                        openItemCountDialog(); // Open count dialog
                    }
                });
            }

            /* List all feeds */
            ListView feedsList = (ListView) findViewById(R.id.widget_feed_list);;
            feedsList.setAdapter(new FeedAdapter(this, feeds));
            feedsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RssFeed feed = (RssFeed)parent.getAdapter().getItem(position); // Get the clicked feed
                    chosenId = feed.getId();
                    openItemCountDialog(); // Open count dialog
                }
            });
        }
    }

    /**
     * Opens a count dialog that lets the user choose how many
     * articles the widget should display.
     */
    private void openItemCountDialog(){
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.widget_itemcount_dialog, null);
        final RadioGroup rg = (RadioGroup)dialogLayout.findViewById(R.id.widget_item_count);
        final AlertDialog itemCount = new AlertDialog.Builder(this)
                .setTitle("Number of items displayed")
                .setView(dialogLayout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // Configurations done
                        RadioButton rb = (RadioButton)dialogLayout.findViewById(
                                rg.getCheckedRadioButtonId());
                        chosenCount = Integer.parseInt(rb.getText().toString());
                        PreferencesHandler.getInstance(WidgetConfigActivity.this).
                                addWidget(widgetId, chosenId, chosenCount, true); // Create widget preferences
                        updateWidgets(); // Update the widget
                        finishConfigs(); // Close the configuration activity
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show(); // Show the dialog

    }

    /**
     * Tell widget to update.
     */
    private void updateWidgets(){
        Intent intent = new Intent(this, RssWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {widgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    /**
     * Finish this activity.
     */
    private void finishConfigs(){
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }
}
