package dv606.rw222ci.rssreader;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter used for listing article items.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class ArticleAdapter extends ArrayAdapter<RssFeedArticle> {
    private Context context;
    private RssDBHandler rssDB;

    public ArticleAdapter(Context context, List<RssFeedArticle> feeds) {
        super(context, R.layout.rss_feed_article_item, R.id.feed_article_title, feeds);
        this.context = context;
        rssDB = RssDBHandler.getInstance(context); // Access database
    }

    public View getView(int position, View view, ViewGroup parent) {
        RssFeedArticle rfa = getItem(position);

        View v;
        /* Re-use view if possible */
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.rss_feed_article_item, parent, false);
        } else
            v = view;

        /* Set the article title */
        TextView titleView = (TextView) v.findViewById(R.id.feed_article_title);
        titleView.setText(rfa.getTitle());
        if(!rfa.isRead())
            titleView.setTypeface(null, Typeface.BOLD); // Bold font if unread
        else
            titleView.setTypeface(null, Typeface.NORMAL); // Normal font if read

        /* Set the (publish) date of the article */
        TextView dateView = (TextView) v.findViewById(R.id.feed_article_date);
        dateView.setText(rfa.getPublishDate());

        /* Set the "favorite" icon */
        ImageView favoriteIcon = (ImageView)v.findViewById(R.id.favorite_icon);
        if(rfa.isFavorite())
            favoriteIcon.setVisibility(View.VISIBLE); // Display the icon if article is marked as favorite
        else
            favoriteIcon.setVisibility(View.INVISIBLE); // Hide the icon if not

        /* Set the icon of the feed to which the article belongs */
        ImageView feedIcon = (ImageView)v.findViewById(R.id.feed_article_image);
        if(rssDB.getFeed(rfa.getParentId()).getIcon() != null){
            String filename = context.getFilesDir() + rssDB.getFeed(rfa.getParentId()).getIcon();
            Drawable d = Drawable.createFromPath(filename); // Get file from memory
            feedIcon.setImageDrawable(d);
        }
        else
            feedIcon.setImageResource(R.drawable.feediconmissing); // No feed image available

        return v;
    }

}
