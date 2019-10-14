package dv606.rw222ci.rssreader;

/**
 * Class corresponding to an RSS feed item.<br><br>
 * An RssFeed instance contains data related to
 * a specific feed.<br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssFeed implements RssItem{

    /* Various feed data fields.*/
    private int id;
    private String title;
    private String urlString;
    private String date;
    private String iconName;

    /**
     * Create a new, empty feed instance.
     */
    public RssFeed(){}

    /**
     * Create a new feed instance with a title and a url.
     * @param title The title of the feed.
     * @param urlString The url to the feed.
     */
    public RssFeed(String title, String urlString){
        setTitle(title);
        setUrlString(urlString);
    }

    /**
     * Get the id of this feed.
     * @return The id of this feed.
     */
    public int getId(){ return id; }

    /**
     * Get the title of this feed.
     * @return The title of this feed.
     */
    public String getTitle(){ return title; }

    /**
     * Get the URL (as a string) of this feed.<br>
     * @return The URL (as a string) of this feed.
     */
    public String getUrlString(){ return urlString; }

    /**
     * Get the subscription date of this feed.
     * @return The subscription date of this feed.
     */
    public String getSubscriptionDate(){ return date; }

    /**
     * Get the icon file name of this feed.<br>
     * This icon may be used as a graphical representation of this feed.
     * @return The icon file path of this feed.
     */
    public String getIcon(){ return iconName;}

    /**
     * Set the id of this feed.
     * @param id The new id of this feed.
     */
    public void setId(int id){ this.id = id; }

    /**
     * Set the title of this feed.
     * @param title The new title of this feed.
     */
    public void setTitle(String title){ this.title = title; }

    /**
     * Set the URL (as a string) of this feed<br>
     * The URL should be the address to an RSS's XML-file.
     * @param urlString The URL (as a string) of this feed.
     */
    public void setUrlString(String urlString){ this.urlString = urlString; }

    /**
     * Set the subscription date of this feed.
     * @param date The subscription date of this feed.
     */
    public void setSubscriptionDate(String date){ this.date = date; }

    /**
     * Set the icon file name for this feed.<br>
     * This should merely be the name of the icon file (including extension).<br>
     * For example: "FeedIcon.ico"
     * @param iconName The name of the icon.
     */
    public void setIcon(String iconName){ this.iconName = iconName; }
}
