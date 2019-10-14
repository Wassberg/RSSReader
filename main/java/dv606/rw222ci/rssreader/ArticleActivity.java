package dv606.rw222ci.rssreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Activity used to display a single RSS article.<br>
 * This activity displays the article's title, an image
 * and description related to it, and a clickable
 * text field redirecting the user to the actual article
 * via the default URL handler (default browser).<br><br>
 *
 * Occasionally, the image displayed may not be the "correct" one.
 * Sometimes, there may be more than one image related to an
 * article. Here, only the first image found in the description
 * tag of the article item is displayed. <br><br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-29
 */
public class ArticleActivity extends Activity {

    private RssDBHandler rssDB;

    private static final String TAG = "ArticleActivity";

    private final int DEFAULT_ARTICLE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view_layout);
        rssDB = RssDBHandler.getInstance(this); // Get access to database

        /* Get the article to display */
        int articleId = getIntent().getIntExtra(getString(R.string.key_article_id), DEFAULT_ARTICLE_ID);
        final RssFeedArticle rfa = rssDB.getArticle(articleId); // Get the article from the database

        if (rfa != null) {
            rssDB.updateArticleIsRead(rfa.getId(), true); // Update the clicked article to a "read" state

            Log.d(TAG, "Article " + articleId + " opened.");

            /* Set the activity title to the name of the feed the article belongs to */
            setTitle(rssDB.getFeed(rfa.getParentId()).getTitle());

            /* Get all views */
            TextView titleView = (TextView) findViewById(R.id.article_view_title);
            TextView descriptionView = (TextView) findViewById(R.id.article_view_description);
            ImageView articleImage = (ImageView) findViewById(R.id.article_view_image);
            TextView articleLink = (TextView) findViewById(R.id.article_view_link);

            /* Set article title */
            titleView.setText(Html.fromHtml(rfa.getTitle()));

            if (rfa.getDescription() != null) { // Only if description exists
                String fullDescription = rfa.getDescription();
                String imageTag = parseImageTagFromDescription(fullDescription); // Get first tag containing
                                                                                 // an image
                String image = parseImageFromImageTag(imageTag); // Get the image from the tag
                String descriptionWithoutImage = fullDescription;
                String removableImage;
                /* Remove all image tags */
                while(descriptionWithoutImage.contains("<img")){
                    removableImage = parseImageTagFromDescription(descriptionWithoutImage);
                    descriptionWithoutImage = descriptionWithoutImage.replace(removableImage, "");
                }

                /* Set article description */
                descriptionView.setText(Html.fromHtml(descriptionWithoutImage));

                /* Set article image (if any) */
                if(!image.equals(""))
                    Glide.with(this)
                        .load(image)
                        .crossFade()
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(articleImage);
            }

            /* Set article link */
            articleLink.setText(Html.fromHtml("<u>" + articleLink.getText() + "</u>"));
            articleLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* Go to article with default browser */
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW);
                    browseIntent.setData(Uri.parse(rfa.getUrlString()));
                    startActivity(browseIntent);
                }
            });

        }
    }

    /**
     * Parses the first occurring image tag (if any) in the input string.
     * @param fullDescription The input string from which the image tag should be parsed.
     * @return The first occurring image tag (if any)
     */
    private String parseImageTagFromDescription(String fullDescription){
        String imageTag;
        String start = "<img"; // Start of image tag
        String end = ">"; // End of image tag
        try {
            if (fullDescription != null && fullDescription.contains(start)) {
                imageTag = fullDescription.substring(
                        fullDescription.indexOf(start)); // Remove anything in front of "<img"
                imageTag = imageTag.substring(0, imageTag.indexOf(end) + 1); // Remove anything past '>'
                return imageTag;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Parses the URL of an image in an image tag.
     * @param imageTag The image tag containing an image URL
     * @return The URL of an image.
     */
    private String parseImageFromImageTag(String imageTag){
        String image;
        String urlStart = "src=\"";
        try{
                image = imageTag.substring(
                        imageTag.indexOf(urlStart) + 5); // Remove anything before the start of the URL
                image = image.substring(0, image.indexOf("\"")); // Remove anything past the end of the URL

                return image.trim(); // Remove any leading/trailing whitespaces
            }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
