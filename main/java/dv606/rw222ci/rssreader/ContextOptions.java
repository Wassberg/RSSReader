package dv606.rw222ci.rssreader;

/**
 * Enum containing various context alternatives.<br>
 * Used to avoid creating separate context constants
 * for multiple activities.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public enum ContextOptions {
    MARK_AS_READ,
    MARK_AS_UNREAD,
    FAVORITE,
    NON_FAVORITE,
    RENAME,
    DELETE
}
