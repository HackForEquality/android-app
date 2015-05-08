package ie.yesequality.yesequality;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ie.yesequality.yesequality.broadcastreceivers.NotificationReceiver;


public class NotificationActivity extends ActionBarActivity {

    public static final String ON_DAY = "on_day";
    public static final String ON_DAY_BEFORE = "on_day_before";
    public static final int ALARM_ID_ON_DAY = 220315;
    public static final int ALARM_ID_ON_DAY_BEFORE = 210315;
    private static final String PREFS = "REMINDERS";
    @InjectView(R.id.switchDayBefore)
    protected SwitchCompat dayBefore;
    @InjectView(R.id.switchOnDay)
    protected SwitchCompat onDay;
    @InjectView(R.id.tvOnDay)
    protected TextView tvOnDay;
    @InjectView(R.id.tvDayBefore)
    protected TextView tvDayBefore;
    private AlarmManager alarmManagerOnDay;
    private AlarmManager alarmManagerOnDayBefore;
    private PendingIntent alarmIntentOnDay;
    private PendingIntent alarmIntentOnDayBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.inject(this);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_cyan)));

        final SharedPreferences notificationPrefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = notificationPrefs.edit();
        onDay.setChecked(notificationPrefs.getBoolean(ON_DAY, false));
        dayBefore.setChecked(notificationPrefs.getBoolean(ON_DAY_BEFORE, false));

        onDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(ON_DAY, isChecked);
                editor.apply();
                if (isChecked) {
                    registerAlarm(true);
                } else {
                    cancelAlarm(true);
                }
            }
        });

        tvOnDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDay.toggle();
            }
        });

        dayBefore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(ON_DAY_BEFORE, isChecked);
                editor.apply();
                if (isChecked) {
                    registerAlarm(false);
                } else {
                    cancelAlarm(false);
                }
            }
        });
        tvDayBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayBefore.toggle();
            }
        });

        alarmManagerOnDay = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManagerOnDayBefore = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    }

    private void registerAlarm(boolean onDay) {

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(ON_DAY, onDay);

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
            alarmIntentOnDay = PendingIntent.getBroadcast(this, ALARM_ID_ON_DAY, intent, 0);
            alarmManagerOnDay.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDay);
            Log.i("NOTIFICATIONACTIVITY", "Setting up Alarm for voting day on: " + dayBeforeVotingDay.getTime());
            Toast.makeText(this, R.string.alarmSetDay, Toast.LENGTH_SHORT).show();
        }
        else {
            // Register alarm for day before voting day
            alarmIntentOnDayBefore = PendingIntent.getBroadcast(this, ALARM_ID_ON_DAY_BEFORE, intent, 0);
            alarmManagerOnDayBefore.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDayBefore);
            Log.i("NOTIFICATIONACTIVITY", "Setting up Alarm for day before voting day on: " + dayBeforeVotingDay.getTime());
            Toast.makeText(this, R.string.alarmSetDayBefore, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(boolean onDay) {

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(ON_DAY, onDay);

        if (onDay){
            if (alarmManagerOnDay != null) {
                if (alarmIntentOnDay == null){
                    alarmIntentOnDay = PendingIntent.getBroadcast(this, ALARM_ID_ON_DAY, intent, 0);
                }
                alarmManagerOnDay.cancel(alarmIntentOnDay);
                Log.i("NOTIFICATIONACTIVITY", "Canceled Alarm for voting day");
                Toast.makeText(this, R.string.alarmCancelDay, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (alarmManagerOnDayBefore != null) {
                if (alarmIntentOnDayBefore == null){
                    alarmIntentOnDayBefore = PendingIntent.getBroadcast(this, ALARM_ID_ON_DAY_BEFORE, intent, 0);
                }
                alarmManagerOnDayBefore.cancel(alarmIntentOnDayBefore);
                Log.i("NOTIFICATIONACTIVITY", "Canceled Alarm for day before voting day");
                Toast.makeText(this, R.string.alarmCancelDayBefore, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
