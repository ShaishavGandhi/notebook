package com.apps.shaishav.notebook.activity;

/**
 * Created by Shaishav on 30-06-2015.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.apps.shaishav.notebook.service.UpdateService;
import com.parse.Parse;

import java.util.Calendar;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "4125dnRjrZ8iQdnII3NQJ51pdXCVkOre0c56bGYT", "CWBaHF2oWjr8aMblz8OxGr7OW1QdlgvRk5YLxpx3");

        Intent updateIntent = new Intent(getApplicationContext(),UpdateService.class);
        Calendar cal = Calendar.getInstance();
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),1234,updateIntent,0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_HALF_DAY,pendingIntent);

    }

}
