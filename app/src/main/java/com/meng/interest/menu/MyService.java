package com.meng.interest.menu;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import xml.NewAppWidget;

public class MyService extends Service {
    private Timer            mTimer;
    private SimpleDateFormat mSimpleDateFormat;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);
    }

    private void updateTime() {
        String time = mSimpleDateFormat.format(new Date());
        //widget上的view
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.new_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_time, time);

        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName name = new ComponentName(getApplicationContext(), NewAppWidget.class);
        //刷新
        manager.updateAppWidget(name, remoteViews);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimer = null;
    }
}
