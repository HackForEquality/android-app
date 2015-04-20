package ie.yesequality.yesequality.broadcastreceivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ie.yesequality.yesequality.CameraMainActivity;
import ie.yesequality.yesequality.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION = 123;
    public static final String ON_DAY = "on_day";
    private NotificationManager mNM;

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean onDay = intent.getBooleanExtra(ON_DAY, true);

        mNM = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        CharSequence title = "YES EQUALITY";
        int icon = R.drawable.ic_yes_icon;
        CharSequence text = "Don't forget to vote!";
        if (onDay) {
            text = "Remind your friends to vote YES tomorrow!";
        }
        long time = System.currentTimeMillis();

        Notification notification = new Notification(icon, text, time);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, CameraMainActivity.class), 0);
        notification.setLatestEventInfo(context, title, text, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNM.notify(NOTIFICATION, notification);
    }
}
