package dv606.rw222ci.rssreader;

/**
 * Class corresponding to an RSS article item.<br><br>
 * An RssFeedArticle instance contains data related to
 * a specific article.<br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssFeedArticle implements RssItem {

    /* Various article data fields */
    private int id;
    private int parentFeedId;
    private String title;
    private String description;
    private String publishDate;
    private String articleUrl;
    private String imageUrl;
    private boolean isFavorite = false;
    private boolean isRead = false;

    /**
     * Create a new, empty article instance.
     */
    public RssFeedArticle(){
    }

    /**
     * Creates an article with a given parent feed id, title,
     * pubish date, article URL and image URL.
     * @param parentFeedId The id of the feed which this article should belong to.
     * @param title The title of this article.
     * @param publishDate The publish date of this article.
     * @param articleUrl The URL to this article.
     * @param imageUrl The URL to an image in this article.
     */
    public RssFeedArticle(int parentFeedId, String title, String publishDate, String articleUrl, String imageUrl){
        this.title = title;
        this.parentFeedId = parentFeedId;
        this.publishDate = publishDate;
        this.articleUrl = articleUrl;
        this.imageUrl = imageUrl;
    }

    /**
     * Set the id of this article.
     * @param id The new id of this article.
     */
    public void setId(int id){ this.id = id; }

    /**
     * Set the id of the feed this article belongs to.
     * @param parentFeedId The parent feed id of this article.
     */
    public void setParentFeedId(int parentFeedId){ this.parentFeedId = parentFeedId; }

    /**
     * Set the title of this article.
     * @param title The new title of this article.
     */
    public void setTitle(String title){ this.title = title; }

    /**
     * Set the publish date of this article.
     * @param publishDate The publish date of this article.
     */
    public void setPublishDate(String publishDate){ this.publishDate = publishDate; }

    /**
     * Sets the URL (as a string) of this article.<br>
     * The URL should be the address to this particular article.
     * @param urlString The URL (as a string) of this article.
     */
    public void setUrlString(String urlString) { this.articleUrl = urlString; }

    /**
     * Set the image URL (as a string) of this article.<br>
     * The URL should be the address to an image connected to this particular article.
     * @param imageUrl The image URL (as a string) of this article.
     */
    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl;}

    /**
     * Set the description of this article.<br>
     * The description should be a textual description of this article,
     * e.g. the paragraph within the "description" tags in an RSS "item".
     * @param description The description of this article.
     */
    public void setDescription(String description){ this.description = description; }

    /**
     * Set whether or not this article should be marked as favorite.
     * @param isFavorite <code>true</code> marks this article as a favorite;
     *                   <code>false</code> marks it as a non-favorite.
     */
    public void setIsFavorite(boolean isFavorite){ this.isFavorite = isFavorite; }

    /**
     * Set whether or not this article should be marked as read.
     * @param isRead <code>true</code> marks this article as read;
     *               <code>false</code> marks it as unread.
     */
    public void setIsRead(boolean isRead){ this.isRead = isRead; }

    /**
     * Get the id of this article.
     * @return The id of this article.
     */
    public int getId(){ return id; }

    /**
     * Get the id of the feed to which this article belongs.
     * @return The id of the "parent" feed.
     */
    public int getParentId(){ return parentFeedId; }

    /**
     * Get the string of this article.
     * @return The string of this article.
     */
    public String getTitle(){ return title; }

    /**
     * Get the publish date of this article.
     * @return The publish of this article.
     */
    public String getPublishDate(){ return publishDate; }

    /**
     * Get the URL (as a string) to this article.
     * @return The URL (as a string) to this article.
     */
    public String getUrlString(){ return articleUrl; }

    /**
     * Get the URL (as a string) of an image connected to this article.
     * @return The URL (as a string) of an image connected to this article.
     */
    public String getImageUrl(){ return imageUrl; }

    /**
     * Get the description of this article.
     * @return The description of this article.
     */
    public String getDescription(){ return description; }

    /**
     * Returns whether or not this article is marked as a favorite.
     * @return <code>true</code>, if this article is a favorite;
     * <code>false</code> otherwise.
     */
    public boolean isFavorite(){ return isFavorite; }

    /**
     * Returns whether or not this article is marked as read.
     * @return <code>true</code>, if this article is read;
     * <code>false</code> otherwise.
     */
    public boolean isRead(){ return isRead; }

}
