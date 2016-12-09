package xml;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.meng.interest.menu.MyService;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //刷新的时候执行  通过RemoteViews 和 AppWidgetManager进行刷新的
    }

    @Override
    public void onEnabled(Context context) {
        //第一个widget添加到桌面的时候调用
        //添加到屏幕上的时候启动sevice
        context.startService(new Intent(context, MyService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onDisabled(Context context) {
        //最后一个widget删除的时候调用  关闭service
        context.stopService(new Intent(context, MyService.class));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //widget删除的时候调用
    }
}

