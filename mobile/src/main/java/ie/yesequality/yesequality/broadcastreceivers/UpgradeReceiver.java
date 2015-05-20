package ie.yesequality.yesequality.broadcastreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class UpgradeReceiver extends BroadcastReceiver {
    private static final String PREFS = "REMINDERS";
    public static final String ON_DAY = "on_day";
    public static final String ON_DAY_BEFORE = "on_day_before";
    public static final int ALARM_ID_ON_DAY = 220315;
    public static final int ALARM_ID_ON_DAY_BEFORE = 210315;


    public UpgradeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {
            registerAlarm(context);
        }
    }
    private void registerAlarm(Context context) {

        final SharedPreferences notificationPrefs = context.getSharedPreferences(PREFS, context.MODE_PRIVATE);
        boolean onDay = notificationPrefs.getBoolean(ON_DAY, false);
        boolean onDayBefore = notificationPrefs.getBoolean(ON_DAY_BEFORE, false);

        AlarmManager alarmManagerOnDay = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager alarmManagerOnDayBefore = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        Calendar dayBeforeVotingDay = Calendar.getInstance();
        dayBeforeVotingDay.set(Calendar.YEAR, 2015);
        dayBeforeVotingDay.set(Calendar.MONTH, 4);
        dayBeforeVotingDay.set(Calendar.DAY_OF_MONTH, 21);
        dayBeforeVotingDay.set(Calendar.HOUR_OF_DAY, 10);
        dayBeforeVotingDay.set(Calendar.MINUTE, 30);
        dayBeforeVotingDay.set(Calendar.SECOND, 0);

        if (onDay){
            // Register alarm for voting day (before day + 1)
            dayBeforeVotingDay.add(Calendar.DATE, 1);
            PendingIntent alarmIntentOnDay = PendingIntent.getBroadcast(context, ALARM_ID_ON_DAY, intent, 0);
            alarmManagerOnDay.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDay);
            Log.i("UPGRADERECEIVER", "Setting up Alarm for voting day on: " + dayBeforeVotingDay.getTime());
        }
        if (onDayBefore) {
            // Register alarm for day before voting day
            PendingIntent alarmIntentOnDayBefore = PendingIntent.getBroadcast(context, ALARM_ID_ON_DAY_BEFORE, intent, 0);
            alarmManagerOnDayBefore.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDayBefore);
            Log.i("UPGRADERECEIVER", "Setting up Alarm for day before voting day on: " + dayBeforeVotingDay.getTime());
        }
    }


}
