package com.yourewinner.yourewinner;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.AppWidgetTarget;

public class RotatingBannerWidget extends AppWidgetProvider {
    private final static String ACTION_LOAD_BANNER = "com.yourewinner.intent.filter.action.LOAD_BANNER";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId: appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.rotating_banner_widget);
            Intent i = new Intent(context, RotatingBannerWidget.class);
            i.setAction(ACTION_LOAD_BANNER);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.rotating_banner, pi);
            loadBanner(context, rv, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(ACTION_LOAD_BANNER)) {
            updateWidgets(context);
        }
    }

    public static void loadBanner(Context context, RemoteViews rv, int appWidgetId) {
        AppWidgetTarget target = new AppWidgetTarget(context, rv, R.id.rotating_banner, appWidgetId);
        Glide.with(context.getApplicationContext())
                .load("https://yourewinner.com/banners/steevbanner.php")
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(target);
    }

    public static void updateWidgets(Context context) {
        ComponentName myWidget = new ComponentName(context, RotatingBannerWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(myWidget);

        for (int appwidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.rotating_banner_widget);
            loadBanner(context, rv, appwidgetId);
            manager.updateAppWidget(appwidgetId, rv);
        }
    }
}
