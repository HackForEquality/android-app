package ie.yesequality.yesequality;

import android.app.Application;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * An application class to hold some utilities
 * Created by rory on 16/05/15.
 */
public class YesEqualityApplication extends Application {
    private static final String VOTE_START = "2015-05-22T00:00:00";
    //private static final String VOTE_START = "2015-05-17T00:08:00";

    private static final String VOTE_END = "2015-05-22T23:00:00";

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DUBLIN_TIMEZONE_NAME = "Europe/Dublin";
    public static Date sVoteStartDate = new Date();
    public static Date sVoteEndDate = new Date();

    @Override
    public void onCreate() {
        super.onCreate();
        SimpleDateFormat isoFormat = new SimpleDateFormat(TIME_FORMAT);
        TimeZone timeZone = TimeZone.getTimeZone(DUBLIN_TIMEZONE_NAME);
        isoFormat.setTimeZone(timeZone);
        Log.d(this.getClass().getSimpleName(), sVoteStartDate.toLocaleString());
        try {
            sVoteStartDate = isoFormat.parse(VOTE_START);
            sVoteEndDate = isoFormat.parse(VOTE_END);
        } catch (ParseException e) {
            sVoteStartDate.setTime(1432249200000l);
            sVoteEndDate.setTime(1432335600000l);
        }
        Log.d(this.getClass().getSimpleName(), sVoteStartDate.toLocaleString());
    }


    public boolean isVotingStarted() {
        Date currentTime = new Date();

        return sVoteStartDate.before(currentTime);
    }

    public boolean isVotingEnded() {
        Date currentTime = new Date();

        return sVoteEndDate.before(currentTime);
    }
}
