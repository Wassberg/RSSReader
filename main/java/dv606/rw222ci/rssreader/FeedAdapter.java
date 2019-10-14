package dv606.rw222ci.rssreader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter used for listing feeds.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class FeedAdapter extends ArrayAdapter<RssFeed> {
    private Context context;
    RssDBHandler rssDB;

    public FeedAdapter(Context context, List<RssFeed> feeds) {
        super(context, R.layout.rss_feed_item, R.id.feed_title, feeds);
        this.context = context;
        rssDB = RssDBHandler.getInstance(context); // Access database
    }

    public View getView(int position, View view, ViewGroup parent) {
        RssFeed rf = rssDB.getFeed(getItem(position).getId());

        View v;
        /* Re-use view if possible */
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.rss_feed_item, parent, false);
        } else
            v = view;

        /* Set the feed title */
        TextView titleView = (TextView) v.findViewById(R.id.feed_title);
        titleView.setText(Html.fromHtml(rf.getTitle()));

        /* Set the feed icon */
        ImageView feedIcon = (ImageView) v.findViewById(R.id.feed_image);
        if (rf.getIcon() != null) {
            String filename = context.getFilesDir() + rf.getIcon();
            Drawable d = Drawable.createFromPath(filename);
            feedIcon.setImageDrawable(d); // Get icon from memory
        } else
            feedIcon.setImageResource(R.drawable.feediconmissing);

        /* Set URL view */
        TextView urlView = (TextView) v.findViewById(R.id.feed_url);
        urlView.setText(rf.getUrlString());

        return v;
    }
}
