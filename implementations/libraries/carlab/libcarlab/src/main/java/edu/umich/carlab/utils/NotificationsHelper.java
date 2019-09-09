package edu.umich.carlab.utils;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import edu.umich.carlab.Constants;
import edu.umich.carlab.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static edu.umich.carlab.Constants.CARLAB_NOTIFICATION_ID;

/**
 * Notification helper functions.
 * <p>
 * This contains an ENUM for the most common notifications. No one has to call the full on flexible
 * notification. function.
 */


public class NotificationsHelper {
    public static NotificationManager notificationManager = null;

    ;
    static NotificationChannel channel;

    private static Notification makeNotification(Context cl, Notifications notificationState) {
        String title, text;
        int icon;
        boolean ongoing = true, notify = false, vibrate = false;
        switch (notificationState) {
            case DISCOVERY_ERROR:
                title = "Connection Error";
                text = "Unable to start data collection.";
                icon = R.drawable.discovery_error;
                break;
            case DISCOVERY_FAIL:
                title = "Disconnected";
                text = "Data collection not running.";
                icon = R.drawable.discovery_fail;
                break;
            case DISCOVERY:
                title = "Connecting";
                text = "Attempting to start data collection.";
                icon = R.drawable.discovery;
                break;
            case STARTING:
                title = "Connected";
                text = "Data collection initiated.";
                icon = R.drawable.starting;
                break;
            case COLLECTING_DATA:
                title = "Running";
                text = "Data collection currently running.";
                icon = R.drawable.collecting_data;
                break;
            case UPLOADING:
                title = "Uploading";
                text = String.format("Uploading collected data. %d data points remaining.", Constants.RemainingDataCount);
                icon = R.drawable.uploading;
                break;
            case STOPPING:
                title = "Stopped";
                text = "Stopped data collection.";
                icon = R.drawable.stopping;
                break;
            default:
                return null;
        }

        return makeNotification(cl, title, text, icon, ongoing, notify, vibrate);
    }

    public static void setNotification(Context context, Notifications notificationState) {
        Notification notification = makeNotification(context, notificationState);
        notificationManager.notify(CARLAB_NOTIFICATION_ID, notification);
    }

    public static void setNotificationForeground(Service cl, Notifications notificationState) {
        Notification notification = makeNotification(cl, notificationState);
        cl.startForeground(CARLAB_NOTIFICATION_ID, notification);
    }

    private static Notification makeNotification(Context ctx, String contentTitle, String contentText, int icon, boolean ongoing, boolean notify, boolean vibrate) {
        if (notificationManager == null)
            notificationManager =
                    (NotificationManager)
                            ctx.getSystemService(NOTIFICATION_SERVICE);


        if (channel == null && Build.VERSION.SDK_INT >= 26) {
            String id = Constants.Notification_Channel;
            // The user-visible name of the channel.
            CharSequence name = "CarLab";

            // The user-visible description of the channel.
            String description = "Updates for CarLab.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);

            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);

            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(false);
            notificationManager.createNotificationChannel(mChannel);
        }


        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(ctx)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setSmallIcon(icon);

        Class<?> mainActivityClass = Utilities.getMainActivity(ctx);
        if (mainActivityClass != null) {
            Intent intent = new Intent(ctx, mainActivityClass);
            final PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
            notificationBuilder.setContentIntent(contentIntent);
        }

        if (Build.VERSION.SDK_INT >= 26)
            notificationBuilder.setChannelId(Constants.Notification_Channel);

        notificationBuilder.setOnlyAlertOnce(true);
        // can cancel?
        if (ongoing) {
            notificationBuilder.setOngoing(true);
        } else {
            notificationBuilder.setAutoCancel(true);
        }

        return notificationBuilder.build();
    }

    private static void cancelNotification() {
        notificationManager.cancel(CARLAB_NOTIFICATION_ID);
    }


    public enum Notifications {
        DISCOVERY,
        DISCOVERY_ERROR,
        COLLECTING_DATA,
        STARTING,
        UPLOADING,
        STOPPING,
        DISCOVERY_FAIL
    }
}
