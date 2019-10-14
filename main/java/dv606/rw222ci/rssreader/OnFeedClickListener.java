package dv606.rw222ci.rssreader;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

/**
 * OnClickListener used on feeds.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class OnFeedClickListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RssFeed feed = (RssFeed)parent.getAdapter().getItem(position); // Get the clicked feed

        /* Start FeedActivity */
        Intent intent = new Intent(view.getContext(), FeedActivity.class);
        intent.putExtra(
                view.getResources().getString(R.string.key_feed_id),
                feed.getId()); // Pass feed id
        view.getContext().startActivity(intent);
    }
}
