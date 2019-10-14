package dv606.rw222ci.rssreader;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * The "core" of the RSS reader. <br>
 * This service downloads articles and feed icons,
 * sets auto-update alarms, creates notifications and
 * informs widgets to update.<br><br>
 *
 * Whenever the service is started, it begins by downloading data related to each feed.<br>
 * One feed at a time, an icon is downloaded to represent the feed (if one is missing) and
 * then articles are downloaded from the RSS URL. Currently, this procedure is non-parallel.<br>
 * Once finished downloading (or if the service is interrupted while doing so), an alarm is set
 * to allow automatic updating (this service will be started again automatically later on).<br>
 * Next, a notification is created to inform the user about any new or unread articles.<br>
 * The service finishes by informing any existing widget to update, since new data may have
 * become available. After doing this, the service terminates itself. <br><br>
 *
 * @// TODO: Allow parallel downloading of articles/icons
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssFeedFetcherService extends Service {
    private static MainDownloaderThread mainDownloaderThread;
    private RssDBHandler rdb;

    public static final int NOTIFICATION_ID = 4634; // Unique notification ID

    private static final String TAG = "RssFeedFetcherService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service started.");
        rdb = RssDBHandler.getInstance(RssFeedFetcherService.this); // Access database;

        if(mainDownloaderThread == null || !mainDownloaderThread.isAlive()) { // Don't run multiple downloads at the same time.
            Log.d(TAG, "Starting new downloads");

            mainDownloaderThread = new MainDownloaderThread();
            mainDownloaderThread.start(); // Start main downloader thread
        }
        else
            Log.d(TAG, "Skipped starting new downloads. Downloads are already running.");

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mainDownloaderThread != null && mainDownloaderThread.isAlive())
            mainDownloaderThread.interrupt(); // Stop the downloading
        setUpdateAlarm(); // Set alarm for auto-update
        if(rdb.getUnreadArticleCount() > 0)
            notifyNewFeeds(); // Notify about new feeds
        updateWidgets(); // Update widgets.
    }

    public class MainDownloaderThread extends Thread {

        private static final String TAG = "MainDownloaderThread";

        @Override
        public void run() {
            Log.i(TAG, "Download thread started.");

            ArrayList<RssFeed> rfaList = rdb.getAllFeeds(); // Get all feeds

            /* Download articles from each feed (non-concurrently)
             * and icons (if needed) for the feeds */
            for (RssFeed rf : rfaList) {

                /* First try to download an icon for the feed (if needed) */
                try {
                    if (rf.getIcon() == null) {
                        URL url = new URL(rf.getUrlString());
                        String filename = url.getHost().replace("www.", "") + ".ico"; // Remove "www." and append ".ico"
                        Bitmap bm = getFeedIcon(rf.getUrlString()); // Get the image

                        if (bm != null) {
                            /* Create the file if one was returned */
                            FileOutputStream out = new FileOutputStream(getFilesDir() + filename);
                            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                            rdb.updateFeedIcon(rf.getId(), filename); // Connect image to feed in database
                        } else
                            rdb.updateFeedIcon(rf.getId(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /* Start downloading the articles */
                ArrayList<RssFeedArticle> articles = RssParserHandler.parseFeedItems(rf.getUrlString());
                rdb.addAllArticles(articles, rf.getId());
            }

            Log.i(TAG, "Downloads finished.");
            RssFeedFetcherService.this.stopSelf();
        }
    }

    /**
     * Download a feed icon.
     * @param urlString The URL of the feed.
     */
    private Bitmap getFeedIcon(String urlString) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            try {

                /* Try common /favicon.ico path */
                bitmap = Glide.with(this)
                        .load(url.getProtocol() + "://" + url.getHost() + "/favicon.ico")
                        .asBitmap()
                        .into(16, 16)
                        .get();
            }catch(ExecutionException ee){
                /* Try google command if /favicon.ico failed */
                bitmap = Glide.with(this)
                        .load("http://www.google.com/s2/favicons?domain=" + url.getHost())
                        .asBitmap()
                        .into(16, 16)
                        .get();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Set an alarm used to inform when it's time for the next automatic update.
     */
    private void setUpdateAlarm(){
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent alarm = new Intent(this, UpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, alarm, 0);

        am.cancel(pi); // Cancel any pre-existing alarms.

        PreferencesHandler prefs = PreferencesHandler.getInstance(this);
        int updateTime = prefs.getAutoUpdateInterval();
        /* Set alarm if auto-update time is greater than 0. 0 indicates no auto-update at all */
        if(updateTime > 0) {
            long nextAlarmTime = SystemClock.elapsedRealtime() + updateTime*60000; // Auto-update according
                                                                                   // to preferences.
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextAlarmTime, pi); // Set the alarm
                                                                             // A non-exact alarm is good enough.
        }
    }

    /**
     * Create and display notification about unread/new feeds.
     */
    public void notifyNewFeeds(){
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, RssHomeActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder.setSmallIcon(R.mipmap.rss_feed_icon)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(rdb.getUnreadArticleCount() + " " + getString(R.string.notification_content_text))
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, nBuilder.build()); // Display notification.
    }

    /**
     * Inform any existing widget to update.
     */
    public void updateWidgets(){
        ComponentName widgets = new ComponentName(this, RssWidget.class);
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(widgets);

        Intent refreshIntent = new Intent(
                this.getApplicationContext(), RssWidget.class);
        refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(refreshIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
