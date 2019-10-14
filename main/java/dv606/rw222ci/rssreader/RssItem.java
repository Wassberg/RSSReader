package dv606.rw222ci.rssreader;

/**
 * Interface including certain data an RSS item must have.<br><br>
 * A class implementing this interface should make use of at least
 * an id, a title, and a URL string.<br>
 *
 * This interface is implemented by the RssFeed the RssFeedArticle
 * classes. Sometimes, things may be done without care for which
 * of these two classes an object is an instance of.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public interface RssItem {

    /**
     * Set the id of this item.
     * @param id The new id of this item.
     */
    void setId(int id);

    /**
     * Get the id of this item.
     * @return The id of this item.
     */
    int getId();

    /**
     * Set a title to this item.
     * @param title The new title of this item.
     */
    void setTitle(String title);

    /**
     * Get the title of this item.
     * @return The title of this item.
     */
    String getTitle();

    /**
     * Set the URL (as a string) of this item.
     * @param urlString The URL (as a string) of this item.
     */
    void setUrlString(String urlString);

    /**
     * Get the url (as a string) of this item.
     * @return The url (as a string) of this item.
     */
    String getUrlString();
}
