package dv606.rw222ci.rssreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Handler of the RSS database used by the application.<br><br>
 *
 * Currently, the database consists of two tables; one for feeds
 * and one for feed articles.<br>
 * The <b>Feed table</b> stores information about the feeds
 * (id, title, URL and an icon path).<br>
 * The <b>Article table</b> stores information about the articles from
 * different feeds (id, feed id, title, description, date, URL, URL to
 * an image, favorite state, and read state).<br><br>
 *
 * @// TODO: Clean up this class
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 *
 */
public class RssDBHandler extends SQLiteOpenHelper {
    private static RssDBHandler mInstance;
    private static SQLiteDatabase db;

    /* Database info */
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RSS DB";

    /* Table names */
    private static final String TABLE_FEEDS = "feeds";
    private static final String TABLE_ARTICLES = "articles";

    /* Feed table columns */
    private static final String KEY_FEED_ID = "id";
    private static final String KEY_FEED_TITLE = "title";
    private static final String KEY_FEED_URLSTRING = "urlString";
    private static final String KEY_FEED_ICON = "icon";

    /* Article table columns */
    private static final String KEY_ARTICLE_ID = "id";
    private static final String KEY_ARTICLE_PARENT_ID = "parentId";
    private static final String KEY_ARTICLE_TITLE = "title";
    private static final String KEY_ARTICLE_DESCRIPTION = "description";
    private static final String KEY_ARTICLE_DATE = "date";
    private static final String KEY_ARTICLE_URLSTRING = "urlString";
    private static final String KEY_ARTICLE_IMAGE_URLSTRING = "imageUrlString";
    private static final String KEY_ARTICLE_IS_FAVORITE = "isFavorite";
    private static final String KEY_ARTICLE_IS_READ = "isRead";

    /* String array-representation of Feed table columns */
    private static final String[] FEED_COLUMNS = {
            KEY_FEED_ID,
            KEY_FEED_TITLE,
            KEY_FEED_URLSTRING,
            KEY_FEED_ICON
    };

    /* String array-representation of Article table columns */
    private static final String[] ARTICLE_COLUMNS = {
            KEY_ARTICLE_ID,
            KEY_ARTICLE_PARENT_ID,
            KEY_ARTICLE_TITLE,
            KEY_ARTICLE_DESCRIPTION,
            KEY_ARTICLE_DATE,
            KEY_ARTICLE_URLSTRING,
            KEY_ARTICLE_IMAGE_URLSTRING,
            KEY_ARTICLE_IS_FAVORITE,
            KEY_ARTICLE_IS_READ
    };

    /**
     * Get an instance of this database handler.
     * @param context Some context used to create this handler
     * @return An instance of this handler.
     */
    public static RssDBHandler getInstance(Context context){
        if(mInstance == null) {
            mInstance = new RssDBHandler(context);
        }
        return mInstance;
    }

    /**
     * Create an RssDBHandler instance.
     * @param context Some context required to create the handler.
     */
    private RssDBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /* Create Feed table */
        String CREATE_FEED_TABLE =
                "CREATE TABLE " + TABLE_FEEDS + "( " +
                        FEED_COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        FEED_COLUMNS[1] + " TEXT, " +
                        FEED_COLUMNS[2] + " TEXT, " +
                        FEED_COLUMNS[3] + " TEXT )";
        db.execSQL(CREATE_FEED_TABLE);

        /* Create Article table */
        String CREATE_ARTICLE_TABLE =
                "CREATE TABLE " + TABLE_ARTICLES + "( " +
                        ARTICLE_COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        ARTICLE_COLUMNS[1] + " TEXT, " +
                        ARTICLE_COLUMNS[2] + " TEXT, " +
                        ARTICLE_COLUMNS[3] + " TEXT, " +
                        ARTICLE_COLUMNS[4] + " TEXT, " +
                        ARTICLE_COLUMNS[5] + " TEXT, " +
                        ARTICLE_COLUMNS[6] + " TEXT, " +
                        ARTICLE_COLUMNS[7] + " TEXT, " +
                        ARTICLE_COLUMNS[8] + " TEXT )";
        db.execSQL(CREATE_ARTICLE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
        this.onCreate(db);
    }

    /**
     * Adds a feed to the database if the feed isn't already in it.
     * @param rssFeed The feed to be added.
     * @return True, if the feed was added; false if it was already in the database.
     */
    public boolean addFeed(RssFeed rssFeed){
        if(!isInDatabase(rssFeed) || rssFeed.getUrlString() == null) {

            ContentValues values = new ContentValues();
            values.put(KEY_FEED_TITLE, rssFeed.getTitle());
            values.put(KEY_FEED_URLSTRING, rssFeed.getUrlString());
            values.put(KEY_FEED_ICON, rssFeed.getIcon());

            db.insert(TABLE_FEEDS, null, values);
            return true;
        }
        return false;
    }

    /**
     * Get a feed from the database.
     * @param id The id of the feed to be fetched.
     * @return The feed with the given id, or <code>null</code> if
     * no such feed exists.
     */
    public RssFeed getFeed(int id){
        String query = "SELECT * FROM " + TABLE_FEEDS
                + " WHERE " + KEY_FEED_ID + " = '" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        RssFeed feed = null;
        if(cursor.moveToFirst()){
            feed = new RssFeed();
            feed = getAllFeedData(feed, cursor); // Give the feed its data
        }
        cursor.close();
        return feed;
    }

    /**
     * Get all feeds from the database.
     * @return An <code>ArrayList</code> containing all the feeds in the database.
     * The list will be empty if no feeds exists.
     */
    public ArrayList<RssFeed> getAllFeeds(){
        ArrayList<RssFeed> feeds = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_FEEDS; // Select all

        Cursor cursor = db.rawQuery(query, null);

        RssFeed feed;
        if (cursor.moveToFirst()){
            do{
                feed = new RssFeed();
                feed = getAllFeedData(feed, cursor); // Give the feed its data
                feeds.add(feed); // Add the feed to the list
            }while(cursor.moveToNext()); // Go through all the feeds
        }

        cursor.close();
        return feeds;
    }

    /**
     * Update the title of a given feed.
     * @param feedId The feed to update.
     * @param newFeedTitle The new title of the feed.
     */
    public void updateFeedTitle(int feedId, String newFeedTitle){
        ContentValues values = new ContentValues();
        values.put(KEY_FEED_TITLE, newFeedTitle);
        updateValuesOfFeed(feedId, values);
    }

    /**
     * Update the icon of a given feed.
     * @param feedId The feed to update.
     * @param iconPath The new path to an icon used by the feed.
     */
    public void updateFeedIcon(int feedId ,String iconPath){
        ContentValues values = new ContentValues();
        values.put(KEY_FEED_ICON, iconPath);
        updateValuesOfFeed(feedId, values);
    }

    /**
     * Update values of a given feed.
     * @param feedId The id of the feed to be updated.
     * @param values The new values to update with.
     */
    private void updateValuesOfFeed(int feedId, ContentValues values){
        db.update(TABLE_FEEDS,
                values,
                KEY_FEED_ID + " = " + feedId,
                null);
    }

    /**
     * Remove a feed from the database
     * (doing so will also remove all articles related to this feed).
     * @param feedId The id of the feed to be removed.
     */
    public void removeFeed(int feedId){
        db.delete(TABLE_FEEDS,
                KEY_FEED_ID + " = " + feedId,
                null); // Remove the feed
        for(RssFeedArticle rfa : getAllArticlesFromFeed(feedId)){
            removeArticle(rfa.getId());
        }
    }

    /**
     * Check if a given <code>RssItem</code> exists in the database.
     * @param ri The <code>RssItem</code> to be checked.
     * @return True, if the item is in the database; false otherwise.
     */
    public boolean isInDatabase(RssItem ri){
        String table = "";
        if(ri instanceof RssFeed)
            table = TABLE_FEEDS; // Look in Feed table
        else if(ri instanceof RssFeedArticle)
            table = TABLE_ARTICLES; // Look in Article table

        Cursor cursor = db.rawQuery("SELECT 1 FROM " + table
                + " WHERE urlString = '" + ri.getUrlString() + "'", null);

        if(cursor.getCount() > 0) { // Item is present in database
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }

    /**
     * Adds an article to the database.
     * @param rfa The article to be added.
     * @param parentFeedId The id of the feed to which the article belongs.
     * @return True, if the article was added; false if the article already
     * exists in the database.
     */
    public boolean addArticle(RssFeedArticle rfa, int parentFeedId){

        if(!isInDatabase(rfa) && rfa.getUrlString() != null) {
            ContentValues values = new ContentValues();
            values.put(KEY_ARTICLE_PARENT_ID, parentFeedId);
            values.put(KEY_ARTICLE_TITLE, rfa.getTitle());
            values.put(KEY_ARTICLE_DESCRIPTION, rfa.getDescription());
            values.put(KEY_ARTICLE_DATE, rfa.getPublishDate());
            values.put(KEY_ARTICLE_URLSTRING, rfa.getUrlString());
            values.put(KEY_ARTICLE_IMAGE_URLSTRING, rfa.getImageUrl());
            values.put(KEY_ARTICLE_IS_FAVORITE, String.valueOf(rfa.isFavorite()));
            values.put(KEY_ARTICLE_IS_READ, String.valueOf(rfa.isRead()));

            db.insert(TABLE_ARTICLES, null, values);
            return true;
        }
        return false;
    }

    /**
     * Add a collection of articles to the database.
     * @param articles The list of articles to be added.
     * @param parentFeedId The id of the feed to which the articles belong.
     */
    public void addAllArticles(ArrayList<RssFeedArticle> articles, int parentFeedId){
        for (int i = 0; i < articles.size(); i++) {
            addArticle(articles.get(i), parentFeedId);
        }
    }

    /**
     * Get an article from the database.
     * @param id The id of the article to be fetched.
     * @return The article with the given id, or null if no such
     * article exists.
     */
    public RssFeedArticle getArticle(int id){

        String query = "SELECT * FROM " + TABLE_ARTICLES
                + " WHERE " + KEY_ARTICLE_ID + " = '" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        RssFeedArticle article = null;
        if(cursor.moveToFirst()){
            article = new RssFeedArticle();
            article = getAllArticleData(article, cursor);
        }
        cursor.close();
        return article;
    }

    /**
     * Get all articles with given specifications.
     * @param selection A selection filter to determine which rows to include.
     * @param selectionArgs Possible arguments to the <code>selection</code>.
     * @return An <code>ArrayList</code> containing all articles from the given selection.
     */
    private ArrayList<RssFeedArticle> getAllArticles(String selection, String[] selectionArgs){
        ArrayList<RssFeedArticle> articles = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_ARTICLES,
                ARTICLE_COLUMNS,
                selection,
                selectionArgs,
                null,
                null,
                KEY_ARTICLE_DATE + " DESC"); // Order by newest

        RssFeedArticle article;
        if (cursor.moveToFirst()){
            do{
                article = new RssFeedArticle();
                article = getAllArticleData(article, cursor);

                articles.add(article);
            }while(cursor.moveToNext());
        }

        cursor.close();
        return articles;
    }

    /**
     * Get all articles in the database.
     * @return An <code>ArrayList</code> containing all the
     * articles in the database.
     */
    public ArrayList<RssFeedArticle> getAllArticles(){
        return getAllArticles(null, null);
    }

    /**
     * Get all articles from a specific feed.
     * @param feedId The id of the feed to which the articles should belong.
     * @return An <code>ArrayList</code> containing all the articles
     * related to the feed.
     */
    public ArrayList<RssFeedArticle> getAllArticlesFromFeed(int feedId){
        return getAllArticles(KEY_ARTICLE_PARENT_ID + " = ?", new String[]{String.valueOf(feedId)});
    }

    /**
     * Get the latest articles of a specific feed. If the feed
     * id is less or equal to 0, the latest articles of any
     * feed will be returned instead.
     * @param feedId The id of the feed to which the articles should belong.
     *               If the id is less or equal to 0, no respect will be given
     *               to which feed the articles belong to.
     * @param articleCount The number of articles to get. May be less than the
     *                     desired number if not enough articles exist.
     * @return An <code>ArrayList</code> containing the latest articles of a given feed.
     */
    public ArrayList<RssFeedArticle> getNewestArticles(int feedId, int articleCount){
        String query = "SELECT * FROM " + TABLE_ARTICLES;
        if(feedId >= 0)
            query += " WHERE " + KEY_ARTICLE_PARENT_ID +
                    " = " + feedId;
        query += " ORDER BY " + KEY_ARTICLE_DATE + " DESC LIMIT " + articleCount;

        ArrayList<RssFeedArticle> articles = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);
        RssFeedArticle article;
        if(cursor.moveToFirst()){
            do{
                article = new RssFeedArticle();
                article = getAllArticleData(article, cursor);

                articles.add(article);
            }while(cursor.moveToNext());
        }
        return articles;
    }

    /**
     * Get the number of feeds that are marked as "Unread".
     * @return The number of unread articles.
     */
    public long getUnreadArticleCount(){
        return DatabaseUtils.queryNumEntries(
                db,
                TABLE_ARTICLES,
                KEY_ARTICLE_IS_READ + " = ?",
                new String[]{"false"});
    }

    /**
     * Change the read state of a given article.
     * @param articleId The article to be updated.
     * @param isRead True, to mark the article as read; False to
     *               mark it as unread.
     */
    public void updateArticleIsRead(int articleId, boolean isRead){

        ContentValues values = new ContentValues();
        values.put(KEY_ARTICLE_IS_READ, String.valueOf(isRead));
        updateValuesOfArticle(articleId, values);

    }

    /**
     * Get all articles marked as favorites.
     * @return An <code>ArrayList</code> containing all articles
     * marked as favorites.
     */
    public ArrayList<RssFeedArticle> getFavoriteArticles(){
        return getAllArticles(KEY_ARTICLE_IS_FAVORITE + " = ?", new String[]{"true"});
    }

    /**
     * Change the favorite state of a given article.
     * @param articleId The id of the article to be updated.
     * @param isFavorite True, if the article should be marked as a favorite;
     *                   False, if it should be marked as a non-favorite.
     */
    public void updateArticleIsFavorite(int articleId, boolean isFavorite){
        ContentValues values = new ContentValues();
        values.put(KEY_ARTICLE_IS_FAVORITE, String.valueOf(isFavorite));
        updateValuesOfArticle(articleId, values);
    }

    /**
     * Update the values of a given article.
     * @param articleId The article to be updated.
     * @param values The new values that the article should be updated with.
     */
    private void updateValuesOfArticle(int articleId, ContentValues values){
        db.update(TABLE_ARTICLES,
                values,
                KEY_ARTICLE_ID + " = " + articleId,
                null);
    }

    /**
     * Remove an artilce from the database.
     * @param articleId The id of the article to be removed.
     */
    public void removeArticle(int articleId){
        db.delete(TABLE_ARTICLES,
                KEY_ARTICLE_ID + " = " + articleId,
                null);
    }

    /**
     * Assign all data to a given feed.
     * @param feed The feed to be updated with data.
     * @param cursor The cursor with access to the data.
     * @return The feed with updated data.
     */
    private RssFeed getAllFeedData(RssFeed feed, Cursor cursor){
        feed.setId(cursor.getInt(0));
        feed.setTitle(cursor.getString(1));
        feed.setUrlString(cursor.getString(2));
        feed.setIcon(cursor.getString(3));

        return feed;
    }


    /**
     * Assign all data to a given article.
     * @param article The article to be updated with data.
     * @param cursor The cursor with access to the data.
     * @return The article with updated data.
     */
    private RssFeedArticle getAllArticleData(RssFeedArticle article, Cursor cursor){
        article.setId(cursor.getInt(0));
        article.setParentFeedId(cursor.getInt(1));
        article.setTitle(cursor.getString(2));
        article.setDescription(cursor.getString(3));
        article.setPublishDate(cursor.getString(4));
        article.setUrlString(cursor.getString(5));
        article.setImageUrl(cursor.getString(6));
        article.setIsFavorite(Boolean.valueOf(cursor.getString(7)));
        article.setIsRead(Boolean.valueOf(cursor.getString(8)));

        return article;
    }
}
