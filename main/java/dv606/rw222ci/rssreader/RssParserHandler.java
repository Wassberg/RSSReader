package dv606.rw222ci.rssreader;

import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Class handling the parsing of RSS data.<br><br>
 * Currently, only the most relevant information from a
 * RSS (XML) document is parsed out. <br><br>
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class RssParserHandler extends DefaultHandler {

    private static final String TITLE = "title";
    private static final String ITEM = "item";
    private static final String ARTICLE_LINK = "link";
    private static final String DESCRIPTION = "description";
    private static final String PUBLISH_DATE = "pubdate";


    /* Common date format in RSS feeds */
    private static DateFormat RssPubDate =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private static ArrayList<RssFeedArticle> articles;

    public static ArrayList<RssFeedArticle> parseFeedItems(String urlString)
    {
        articles = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection(); // Connect to URL
            InputStream input = urlConnection.getInputStream();

            /* Create PullParser */
            XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = xppFactory.newPullParser();
            xpp.setInput(input, "UTF_8");

            parseXml(xpp); // Parse the feeds

            input.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return articles;
    }

    private static void parseXml(XmlPullParser xpp) throws XmlPullParserException,
                                                                    IOException {
        boolean inItem = false;
        int eventType = xpp.getEventType();
        RssFeedArticle rfa = new RssFeedArticle();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equalsIgnoreCase(ITEM)) { // Entered an "item" tag
                    inItem = true;
                    rfa = new RssFeedArticle(); // Create a new article instance
                } else if (xpp.getName().equalsIgnoreCase(TITLE)) { // Entered a "title" tag
                    if (inItem)
                        rfa.setTitle(xpp.nextText()); // Set article title
                } else if (xpp.getName().equalsIgnoreCase(PUBLISH_DATE)) { // Entered a "pubdate" tag
                    if (inItem)
                        try {
                            rfa.setPublishDate(formatDate(xpp.nextText())); // Try to format the date
                        }catch (ParseException pe){
                            pe.printStackTrace();
                            rfa.setPublishDate(xpp.nextText()); // Set default date if parsing failed
                        }
                } else if (xpp.getName().equalsIgnoreCase(DESCRIPTION)) { // Entered a "description" tag
                    if (inItem){
                        rfa.setDescription(xpp.nextText()); // Parse out the paragraph
                    }
                }
                else if (xpp.getName().equalsIgnoreCase(ARTICLE_LINK)) { // Entered a "link" tag
                    rfa.setUrlString(xpp.nextText()); // Set article URL
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (xpp.getName().equalsIgnoreCase(ITEM)) { // Exiting an "item"
                    inItem = false;
                    articles.add(rfa); // Add the created article
                }
            }
            eventType = xpp.next();
        }
    }

    /**
     * Tries to format the input string to the format <code>yyyy/MM/dd HH:mm.</code>
     * @param dateString The string to be formatted.
     * @return A formatted version of the input string.
     * @throws ParseException If the input string could not be formatted.
     */
    private static String formatDate(String dateString) throws ParseException{
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        Date d1 = RssPubDate.parse(dateString); // Try to format the date
        return df.format(d1);
    }
}
