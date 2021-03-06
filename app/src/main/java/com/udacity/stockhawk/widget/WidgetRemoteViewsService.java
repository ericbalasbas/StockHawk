/*
 * Copyright (c) 2017. Eric Balasbas
 */

package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;


public class WidgetRemoteViewsService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}


class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor cursor;
    private int mAppWidgetId;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;


    StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // https://developer.android.com/guide/topics/appwidgets/index.html#fresh
    // see Keeping Data Fresh on RemoteViewsFactory lifecycle
    public void onCreate() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        Timber.d("onCreate");
    }


    // http://stackoverflow.com/questions/13187284/android-permission-denial-in-widget-remoteviewsfactory-for-content

    @Override
    public void onDataSetChanged() {

        final long token = Binder.clearCallingIdentity();

        try {
            if (cursor != null) {
                cursor.close();
            }

            ContentResolver resolver = mContext.getContentResolver();

            String[] projection = Contract.Quote.QUOTE_COLUMNS.toArray(new String[] {});
            cursor = resolver.query(Contract.Quote.URI, projection, null, null, null);

            Timber.d("onDataSetChanged: " + Contract.Quote.URI.toString());
            Timber.d("onDataSetChanged: cursor.getCount: " + Integer.toString(cursor.getCount()));
            cursor.moveToFirst();
            Timber.d(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        } finally {
            Binder.restoreCallingIdentity(token);
        }

    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        if (cursor == null) {
            Timber.d("getCount: 0");
            return 0;
        } else {
            Timber.d("getCount: " + Integer.toString(cursor.getCount()));
            return cursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Timber.d("getViewAt: position: " + Integer.toString(position));

        if (cursor != null) {
            cursor.moveToPosition(position);
        } else {
            Timber.d("getViewAt: cursor NULL");
            return null;
        }

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        Float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);

        Timber.d("getViewAt: " + symbol);

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        // Construct a remote views item based on the app widget item XML file,
        // and set the text based on the position.
        // https://developer.android.com/guide/topics/appwidgets/index.html

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        views.setTextViewText(R.id.widget_symbol, symbol);
        views.setTextViewText(R.id.widget_price, dollarFormat.format(price));

        Timber.d("getViewAt: price: " + dollarFormat.format(price));

        // Change remoteView background
        // http://stackoverflow.com/questions/6333774/change-remoteview-imageview-background
        if (rawAbsoluteChange > 0) {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            Timber.d("getViewAt: setBackgroundResource: green");
        } else {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            Timber.d("getViewAt: setBackgroundResource: red");
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.widget_change, change);
            Timber.d("getViewAt: absolute");
        } else {
            views.setTextViewText(R.id.widget_change, percentage);
            Timber.d("getViewAt: percentage");
        }

        // https://developer.android.com/guide/topics/appwidgets/index.html#implementing_collections
        // Setting the fill-in Intent
        // Your RemoteViewsFactory must set a fill-in intent on each item in the collection.
        // This makes it possible to distinguish the individual on-click action of a given item.
        // The fill-in intent is then combined with the PendingIntent template in order to determine
        // the final intent that will be executed when the item is clicked.

         Intent fillInIntent = new Intent();
         fillInIntent.putExtra("stock", symbol);
        // Make it possible to distinguish the individual on-click
        // action of a given item
         views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;
    }


    // https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#getLoadingView()
    // This allows for the use of a custom loading view which appears between the time that getViewAt(int) is called and returns. If null is returned, a default loading view will be used.
    // getLoadingView must be implemented
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}

