package dv606.rw222ci.rssreader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Main activity of the RSS reader.<br><br>
 * This activity contains three different tabs;
 * one for viewing all feeds subscribed to, one for viewing all articles across
 * all feeds, and one for viewing favorite articles.<br>
 * Currently, the change of content listed when pressing a certain
 * tab is done by setting a new adapter to the list.<br>
 * To make it easier to control how the lists update accordingly when pressing
 * the update button (starting the download service), a progress dialog will
 * block the activity. The dialog is only blocking the activity for as long as the updating
 * is active, or until the user decides to cancel it. Either if it finishes by itself
 * or if the user cancels it, the activity will be updated with any new data.<br><br>
 *
 * @// TODO: Change how the list is updated (don't force user back to top)<br>
 * @// TODO: Clean up this class
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssHomeActivity extends Activity {

    private static final String TAG = "RssHomeActivity";

    /* Tab tags */
    private final String OVERVIEW_TAB_TAG = "Overview";
    private final String ALL_TAB_TAG = "All";
    private final String FAVORITES_TAB_TAG = "Favorites";

    ListView lv;

    Toast t;
    ArrayAdapter adapter;
    RssDBHandler rssDB;
    TabLayout.Tab currentTab;
    AlertDialog addFeedDialog;
    ProgressDialog downloadDialog;
    Intent downloadIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, TAG + " started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_home_activity);
        t = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        addFeedDialog = createAddFeedsDialog(); // Create a dialog for adding new feeds
        downloadDialog = createDownloadDialog(); // Create a dialog used when updating
        downloadIntent = new Intent(this, RssFeedFetcherService.class);
        rssDB = RssDBHandler.getInstance(this);

        /* Create navigation tabs */
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        currentTab = tabLayout.newTab()
                .setText(getString(R.string.tab_overview))
                .setTag(OVERVIEW_TAB_TAG);
        tabLayout.addTab(currentTab);
        tabLayout.addTab(tabLayout.newTab()
                .setText(getString(R.string.tab_all))
                .setTag(ALL_TAB_TAG));
        tabLayout.addTab(tabLayout.newTab()
                .setText(getString(R.string.tab_favorites))
                .setTag(FAVORITES_TAB_TAG));
        tabLayout.setOnTabSelectedListener(new TabSelectedListener());


        /* Some start-up feeds (used for faster testing) */
        //RssFeed rf = new RssFeed("Aftonbladet", "http://www.aftonbladet.se/rss.xml");
        //rssDB.addFeed(rf);
        //rf = new RssFeed("Gamereactor", "https://www.gamereactor.se/rss/rss.php?texttype=4");
        //rssDB.addFeed(rf);
        //rf = new RssFeed("Smålandsposten", "http://www.smp.se/feed");
        //rssDB.addFeed(rf);
        //rf = new RssFeed("Washington Post", "http://feeds.washingtonpost.com/rss/rss_powerpost");
        //rssDB.addFeed(rf);

        lv = (ListView)findViewById(R.id.item_lister);
        registerForContextMenu(lv);
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rss_home, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList(); // Update the list whenever returning to the activity
                      // Way to do this should be changed, since it currently causes
                      // the user to end up at the top of the list again.
        cancelNotifications(); // Cancel any notifications when going in to this activity
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_add_feeds)
            addFeedDialog.show(); // Open up dialog for adding feeds

        else if(id == R.id.action_update_feeds) {
            this.startService(downloadIntent); // Start downloader service
            downloadDialog.show(); // Open up progress dialog
            new Thread(new LoaderThread()).start(); // Start the downloader thread
        }

        else if(id == R.id.action_settings){
            this.startActivity(new Intent(this, RssReaderPreferences.class)); // Enter preferences
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        RssItem ri = (RssItem)adapter.getItem(info.position);

        /* Create context menu */
        menu.setHeaderTitle(ri.getTitle());
        if(ri instanceof RssFeedArticle){ // Only do add these if an article is selected
            menu.add(0, ContextOptions.MARK_AS_READ.ordinal(), 0, getString(R.string.context_option_mar));
            menu.add(0, ContextOptions.MARK_AS_UNREAD.ordinal(), 0, getString(R.string.context_option_mau));
            if(!((RssFeedArticle) ri).isFavorite())
                menu.add(0, ContextOptions.FAVORITE.ordinal(), 0, getString(R.string.context_option_fav));
            else
                menu.add(0, ContextOptions.NON_FAVORITE.ordinal(), 0, getString(R.string.context_option_nfav));
        }
        else if(ri instanceof RssFeed) // Only add these if a feed is selected
            menu.add(0, ContextOptions.RENAME.ordinal(), 0, getString(R.string.context_option_ren));

        menu.add(0, ContextOptions.DELETE.ordinal(), 0, getString(R.string.context_option_del));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        RssItem ri = (RssItem)adapter.getItem(info.position);
        if(item.getItemId() == ContextOptions.MARK_AS_READ.ordinal()){
            rssDB.updateArticleIsRead(ri.getId(), true);
            adapter.remove(ri); // Updating like this avoid being sent to the top of the list.
            adapter.insert(rssDB.getArticle(ri.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.MARK_AS_UNREAD.ordinal()){
            rssDB.updateArticleIsRead(ri.getId(), false);
            adapter.remove(ri);
            adapter.insert(rssDB.getArticle(ri.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.FAVORITE.ordinal()) {
            rssDB.updateArticleIsFavorite(ri.getId(), true);
            adapter.remove(ri);
            adapter.insert(rssDB.getArticle(ri.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.NON_FAVORITE.ordinal()) {
            rssDB.updateArticleIsFavorite(ri.getId(), false);
            adapter.remove(ri);
            adapter.insert(rssDB.getArticle(ri.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.RENAME.ordinal()){
            openRenameFeedDialog(ri);
        }
        else if(item.getItemId() == ContextOptions.DELETE.ordinal()){
            adapter.remove(ri);
            if(ri instanceof RssFeedArticle)
                rssDB.removeArticle(ri.getId()); // Remove article from database
            else if(ri instanceof RssFeed)
                rssDB.removeFeed(ri.getId()); // Remove feed from database
        }

        adapter.notifyDataSetChanged(); // Update list with new data
        return super.onContextItemSelected(item);
    }

    private class TabSelectedListener implements TabLayout.OnTabSelectedListener{

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            currentTab = tab;
            updateList(); // Change of tab -> Update list accordingly
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    }

    /**
     * Runs for as long as the download service is running
     * and closes the progress dialog once done.
     */
    private class LoaderThread implements Runnable{
        @Override
        public void run() {
            while(isServiceRunning(RssFeedFetcherService.class)); // Loop till service is not running
            downloadDialog.dismiss();
        }
    }

    /**
     * Create a dialog used when adding a new feed.
     * @return An <code>AlertDialog</code> to be shown when a new feed is to be added.
     */
    public AlertDialog createAddFeedsDialog(){
        LayoutInflater inflater = RssHomeActivity.this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.add_feed_dialog, null);

        final EditText feedTitle = (EditText) dialogLayout.findViewById(R.id.add_feed_title);
        final EditText feedUrl = (EditText) dialogLayout.findViewById(R.id.add_feed_url);

        /* Create the dialog */
        final AlertDialog addDialog = new AlertDialog.Builder(RssHomeActivity.this)
                .setView(dialogLayout)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        /* Set how the dialog should appear when shown. */
        addDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                feedTitle.setText(""); // Clear the title field
                feedUrl.setText(""); // Clear the URL field
                Button addButton = addDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                /* Set what happens when the positive button is pressed */
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = feedTitle.getText().toString();
                        String url = feedUrl.getText().toString();
                        RssFeed newFeed = new RssFeed(title, url);
                        if (newFeed.getUrlString().equals("")) { // If URL is empty
                            t.setText("URL cannot be empty");
                            t.show(); // Inform user
                        } else if (rssDB.isInDatabase(newFeed)) { // If feed exists in database
                            t.setText("Feed already subscribed to");
                            t.show(); // Inform user
                        } else { // Otherwise, add feed to database
                            rssDB.addFeed(newFeed);
                            updateList(); // Update with the new feed
                            dialog.dismiss(); // and close the dialog
                        }
                    }
                });
            }
        });
        return addDialog;
    }

    /**
     * Create a dialog used when updating the activity.
     * @return A <code>ProgressDialog</code> to be shown when updating the activity.
     */
    private ProgressDialog createDownloadDialog(){
        ProgressDialog dialog = new ProgressDialog(RssHomeActivity.this);
        dialog.setMessage("Downloading data...");
        dialog.setCancelable(false); // Don't allow exiting the dialog
        dialog.setCanceledOnTouchOutside(false); // unless the "cancel" button is pressed.
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RssHomeActivity.this.stopService(downloadIntent); // Cancel button pressed -> Stop the downloading service
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateList(); // Update list with any new data
            }
        });
        return dialog;
    }

    /**
     * Creates and opens a dialog used to rename an RssItem.
     * @param ri The RssItem to be renamed.
     */
    private void openRenameFeedDialog(RssItem ri){
        LayoutInflater inflater = RssHomeActivity.this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.rename_feed_dialog, null);
        final EditText newTitle = (EditText) dialogLayout.findViewById(R.id.new_feed_title);
        final int feedId = ri.getId();
        newTitle.setText(ri.getTitle()); // Set the feed's old name in the field

        AlertDialog renameDialog = new AlertDialog.Builder(RssHomeActivity.this)
                .setView(dialogLayout)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rssDB.updateFeedTitle(feedId, newTitle.getText().toString());
                        updateList(); // Update list with new data
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        renameDialog.show(); // Display the dialog
    }

    /**
     * Cancel notification created by the downloader service.
     */
    private void cancelNotifications(){
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(RssFeedFetcherService.NOTIFICATION_ID);
    }

    /**
     * Update the list according to the selected tab.
     */
    private void updateList(){
        if(currentTab.getTag().equals(OVERVIEW_TAB_TAG)) {
            adapter = new FeedAdapter(this, rssDB.getAllFeeds());
            lv.setOnItemClickListener(new OnFeedClickListener());
        }
        else if(currentTab.getTag().equals(ALL_TAB_TAG)) {
            adapter = new ArticleAdapter(this, rssDB.getAllArticles());
            lv.setOnItemClickListener(new OnArticleClickListener());
        }
        else if(currentTab.getTag().equals(FAVORITES_TAB_TAG)) {
            adapter = new ArticleAdapter(this, rssDB.getFavoriteArticles());
            lv.setOnItemClickListener(new OnArticleClickListener());
        }
        lv.setAdapter(adapter);
    }

    /**
     * Check whether a specific service is running or not.
     * @param serviceClass The service class to be running.
     * @return True, if the given service is currently running; false otherwise.
     */
    private boolean isServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())) // Service is running
                return true;
        }
        return false;
    }
}
