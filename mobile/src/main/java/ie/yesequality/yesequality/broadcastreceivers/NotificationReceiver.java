package ie.yesequality.yesequality.broadcastreceivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ie.yesequality.yesequality.R;
import ie.yesequality.yesequality.SplashScreen;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ON_DAY = "on_day";
    private static final int NOTIFICATION = 123;

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean onDay = intent.getBooleanExtra(ON_DAY, true);

        CharSequence title = "YES EQUALITY";
        CharSequence text = "Don't forget to vote!";
        if (onDay) {
            text = "Remind your friends to vote YES tomorrow!";
        }

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_yes_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(text)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, SplashScreen.class), 0))
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION, notification);
    }
}
