package dv606.rw222ci.rssreader;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activity used to display RSS articles from a single feed.<br><br>
 * This activity is simply a list containing articles to
 * one specific feed. <br><br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class FeedActivity extends ListActivity {
    private RssDBHandler rssDB;
    private ListView lv;

    private static final String TAG = "FeedActivity";

    private final int DEFAULT_FEED_ID = 0;

    private ArrayAdapter adapter;
    private int feedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rssDB = RssDBHandler.getInstance(this); // Access database

        /* Get the feed to display */
        feedId = getIntent().getIntExtra(getString(R.string.key_feed_id), DEFAULT_FEED_ID);

        Log.d(TAG, "Feed " + feedId + " opened.");

        /* Set the activity title to the name of the feed */
        setTitle(Html.fromHtml(rssDB.getFeed(feedId).getTitle()));
        lv = this.getListView();
        registerForContextMenu(lv);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* List all articles */
        adapter = new ArticleAdapter(this, rssDB.getAllArticlesFromFeed(feedId));
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnArticleClickListener());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        RssFeedArticle rfa = (RssFeedArticle)adapter.getItem(info.position);

        /* Create context menu */
        menu.setHeaderTitle(rfa.getTitle());
        menu.add(0, ContextOptions.MARK_AS_READ.ordinal(), 0, getString(R.string.context_option_mar));
        menu.add(0, ContextOptions.MARK_AS_UNREAD.ordinal(), 0, getString(R.string.context_option_mau));
        if(rfa.isFavorite())
            menu.add(0, ContextOptions.FAVORITE.ordinal(), 0, getString(R.string.context_option_fav));
        else
            menu.add(0, ContextOptions.NON_FAVORITE.ordinal(), 0, getString(R.string.context_option_nfav));
        menu.add(0, ContextOptions.DELETE.ordinal(), 0, getString(R.string.context_option_del));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        RssFeedArticle rfa = (RssFeedArticle)adapter.getItem(info.position);
        if(item.getItemId() == ContextOptions.MARK_AS_READ.ordinal()){
            rssDB.updateArticleIsRead(rfa.getId(), true);
            adapter.remove(rfa); // Remove un-updated from adapter
            adapter.insert(rssDB.getArticle(rfa.getId()), info.position); // Insert updated in adapter
        }
        else if(item.getItemId() == ContextOptions.MARK_AS_UNREAD.ordinal()){
            rssDB.updateArticleIsRead(rfa.getId(), false);
            adapter.remove(rfa);
            adapter.insert(rssDB.getArticle(rfa.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.FAVORITE.ordinal()) {
            rssDB.updateArticleIsFavorite(rfa.getId(), true);
            adapter.remove(rfa);
            adapter.insert(rssDB.getArticle(rfa.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.NON_FAVORITE.ordinal()) {
            rssDB.updateArticleIsFavorite(rfa.getId(), false);
            adapter.remove(rfa);
            adapter.insert(rssDB.getArticle(rfa.getId()), info.position);
        }
        else if(item.getItemId() == ContextOptions.DELETE.ordinal()){
            adapter.remove(rfa);
            rssDB.removeArticle(rfa.getId());
        }

        adapter.notifyDataSetChanged(); // Update the list
        return super.onContextItemSelected(item);
    }
}
