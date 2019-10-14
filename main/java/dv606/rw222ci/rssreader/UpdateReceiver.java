package dv606.rw222ci.rssreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Broadcast receiver used to listen for alarms.<br>
 * Whenever a set alarm goes of, the downloader service will be
 * started.
 *
 * @author Robin Wassbjer (rw222ci)
 * @since 2015-10-30
 */
public class UpdateReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, RssFeedFetcherService.class);
        context.startService(service);
    }
}
