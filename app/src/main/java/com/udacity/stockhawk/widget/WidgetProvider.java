package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetProvider extends AppWidgetProvider {
    // This is called to update the App Widget at intervals defined by the updatePeriodMillis
    // attribute in the AppWidgetProviderInfo (see Adding the AppWidgetProviderInfo Metadata above).
    // This method is also called when the user adds the App Widget, so it should perform the
    // essential setup, such as define event handlers for Views and start a temporary Service,
    // if necessary. However, if you have declared a configuration Activity, this method is not
    // called when the user adds the App Widget, but is called for the subsequent updates. It is
    // the responsibility of the configuration Activity to perform the first update when
    // configuration is done.
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        // TODO: Test widget_list and check intent values here
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {

            Timber.d("onUpdate: " + Integer.toString(appWidgetId));
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            // Create an Intent to launch MainActivity
//            Intent intent = new Intent(context, MainActivity.class);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            // https://developer.android.com/reference/android/widget/RemoteViews.html#setOnClickPendingIntent(int, android.app.PendingIntent)
            // setting on click action of items in collections will not work with setOnClickPendingIntent
            // use setPendingIntentTemplate instead
            // views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
//            } else {
//                setRemoteAdapterV11(context, views);
//            }
//            boolean useDetailActivity = context.getResources()
//                    .getBoolean(R.bool.use_detail_activity);
//            Intent clickIntentTemplate = useDetailActivity
//                    ? new Intent(context, DetailActivity.class)
//                    : new Intent(context, MainActivity.class);
            // TODO: ?????????????????
//            Intent clickIntentTemplate = new Intent(context, MainActivity.class);
//            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
//                    .addNextIntentWithParentStack(clickIntentTemplate)
//                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    // TODO: Fix SyncAdapter usage here
    // TODO: remove dependency on QuoteSyncJob???
    // android.appwidget.action.APPWIDGET_ENABLED
    // D/WidgetProvider: android.appwidget.action.APPWIDGET_UPDATE

    // https://developer.android.com/guide/topics/appwidgets/index.html
    // This is called for every broadcast and before each of the above callback methods.
    // You normally don't need to implement this method because the default AppWidgetProvider
    // implementation filters all App Widget broadcasts and calls the above methods as appropriate.
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Timber.d("onReceive: " + intent.getAction());
        super.onReceive(context, intent);
//        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
//        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, WidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
//    @SuppressWarnings("deprecation")
//    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
//        views.setRemoteAdapter(0, R.id.widget_list,
//                new Intent(context, WidgetRemoteViewsService.class));
//    }
}