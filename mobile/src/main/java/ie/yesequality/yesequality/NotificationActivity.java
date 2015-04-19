package ie.yesequality.yesequality;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ie.yesequality.yesequality.broadcastreceivers.NotificationReceiver;


public class NotificationActivity extends Activity {

    private static final String PREFS = "REMINDERS";
    public static final String ON_DAY = "on_day";
    public static final String ON_DAY_BEFORE = "on_day_before";

    private AlarmManager alarmManagerOnDay;
    private AlarmManager alarmManagerOnDayBefore;
    private PendingIntent alarmIntentOnDay;
    private PendingIntent alarmIntentOnDayBefore;
    private int intentIdOnDay;
    private int intentIdOnDayBefore;
    @InjectView(R.id.switchDayBefore) Switch dayBefore;
    @InjectView(R.id.switchOnDay) Switch onDay;

    @OnClick(R.id.okButton)
    public void closeScreen() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.inject(this);

        final SharedPreferences notificationPrefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = notificationPrefs.edit();
        onDay.setChecked(notificationPrefs.getBoolean(ON_DAY, true));
        dayBefore.setChecked(notificationPrefs.getBoolean(ON_DAY_BEFORE, true));

        onDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(ON_DAY, isChecked);
                editor.commit();
                if (isChecked){
                    registerAlarm(true);
                }
                else {
                    cancelAlarm(true);
                }
            }
        });

        dayBefore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(ON_DAY_BEFORE, isChecked);
                editor.commit();
                if (isChecked){
                    registerAlarm(false);
                }
                else {
                    cancelAlarm(false);
                }
            }
        });
    }

    private void registerAlarm(boolean onDay) {

        alarmManagerOnDay = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManagerOnDayBefore = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
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
            intentIdOnDay = (int) System.currentTimeMillis();
            alarmIntentOnDay = PendingIntent.getBroadcast(this, intentIdOnDay, intent, 0);
            alarmManagerOnDay.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDay);
        }
        else {
            // Register alarm for day before voting day
            intentIdOnDayBefore = (int) System.currentTimeMillis();
            alarmIntentOnDayBefore = PendingIntent.getBroadcast(this, intentIdOnDayBefore, intent, 0);
            alarmManagerOnDayBefore.set(AlarmManager.RTC_WAKEUP, dayBeforeVotingDay.getTimeInMillis(), alarmIntentOnDayBefore);
        }
    }

    private void cancelAlarm(boolean onDay) {
        if (onDay){
            if (alarmManagerOnDay != null) {
                alarmManagerOnDay.cancel(alarmIntentOnDay);
            }
        }
        else {
            if (alarmManagerOnDayBefore != null) {
                alarmManagerOnDayBefore.cancel(alarmIntentOnDayBefore);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
