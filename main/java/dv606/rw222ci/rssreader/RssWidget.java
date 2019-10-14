package dv606.rw222ci.rssreader;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Class handling the updating and deletion of the RSS reader widgets.<br><br>
 *
 * An RSS reader widget will display a number (chosen during configuration) of
 * articles. The articles are the most recent ones and can belong to one specific feed
 * or be chosen from the latest ones across all feeds. <br>
 * The widget will display the title of the article and an icon to identify which
 * feed it belongs to. Clicking on an article's title in the widget will redirect the user to
 * an <code>ArticleActivity</code> based on the article that was clicked.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssWidget extends AppWidgetProvider {
    private static final String TAG = "RssWidget";

    private RssDBHandler rssDB;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.context = context;
        rssDB = RssDBHandler.getInstance(context); // Access the database

        ComponentName thisWidget = new ComponentName(context, RssWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        ArrayList<RssFeedArticle> articles;

        PreferencesHandler prefs = PreferencesHandler.getInstance(context);

        for(int id : allWidgetIds){ // Update all widgets
            if(prefs.isWidgetConfigured(id)) { // Only update if the widget is configured.
                                               // Widgets tend to update once before the
                                               // configuration activity has even showed up.
                int feedId = prefs.getWidgetFeedId(id); // Get the id of the widget
                int count = prefs.getWidgetArticleCount(id); // Get the number of articles this widget should display
                articles = rssDB.getNewestArticles(feedId, count); // Get latest articles

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rsswidget_layout);
                views.removeAllViews(R.id.widget_items);
                for(RssFeedArticle rfa : articles) { // Create new views for each article
                    RemoteViews newItem = createNewItem(rfa);
                    views.addView(R.id.widget_items, newItem); // Add new view programmatically

                    appWidgetManager.updateAppWidget(id, views);
                }

                Log.i(TAG, "Widget " + id  + " updated!");
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        PreferencesHandler prefs = PreferencesHandler.getInstance(context);
        for(int id : appWidgetIds) {
            prefs.deleteWidget(id);

            Log.i(TAG, "Widget " + id + " deleted!");
        }
    }

    /**
     * Create a new remote view based on the given RssFeedArticle.
     * @param rfa The article the view should be based on.
     * @return The created remote view.
     */
    private RemoteViews createNewItem(RssFeedArticle rfa){
        RemoteViews newItem = new RemoteViews(context.getPackageName(), R.layout.rsswidget_item);
        newItem.setTextViewText(R.id.article_title, Html.fromHtml(rfa.getTitle())); // Set article title
        String filename = context.getFilesDir() + rssDB.getFeed(rfa.getParentId()).getIcon();
        Bitmap bm = BitmapFactory.decodeFile(filename);
        newItem.setImageViewBitmap(R.id.feed_icon, bm); // Set icon of the feed the article belongs to

        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(context.getString(R.string.key_article_id), rfa.getId());
        PendingIntent pi = PendingIntent.getActivity(context, rfa.getId(), intent, 0);

        newItem.setOnClickPendingIntent(R.id.article_title, pi); // Make the ArticleActivity appear
                                                                 // if title was clicked.

        return newItem;
    }
}
