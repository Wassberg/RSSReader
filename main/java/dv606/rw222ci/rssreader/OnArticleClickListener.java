package dv606.rw222ci.rssreader;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

/**
 * OnClickListener used on article items.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class OnArticleClickListener implements AdapterView.OnItemClickListener{

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RssFeedArticle article = (RssFeedArticle)parent.getAdapter().getItem(position); // Get clicked item

        /* Start ArticleActivity */
        Intent intent = new Intent(view.getContext(), ArticleActivity.class);
        intent.putExtra(
                view.getResources().getString(R.string.key_article_id),
                article.getId()); // Pass article id to activity
        view.getContext().startActivity(intent);

        RssDBHandler rssDB = RssDBHandler.getInstance(view.getContext()); // Access database
        ArticleAdapter adapter = (ArticleAdapter)parent.getAdapter();
        adapter.remove(article); // Remove the old article
        adapter.insert(rssDB.getArticle(article.getId()), position); // And replace it with the new, updated one
        adapter.notifyDataSetChanged(); // Tell the adapter to update
    }
}
