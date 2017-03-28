package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import timber.log.Timber;


// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

//    @BindView(R.id.widget_symbol) TextView StockSymbolView;
//    @BindView(R.id.widget_price) TextView PriceView;
//    @BindView(R.id.widget_change) TextView PriceChangeView;

    StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // ??????? why is this never called ?????????
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

    // 03-24 10:35:10.466 2606-2619/com.udacity.stockhawk E/AndroidRuntime: FATAL EXCEPTION: Binder:2606_2
// Process: com.udacity.stockhawk, PID: 2606
// java.lang.SecurityException: Permission Denial: reading com.udacity.stockhawk.data.StockProvider
// uri content://com.udacity.stockhawk/quote from pid=2045, uid=10014 requires the provider be
// exported, or grantUriPermission()
// at android.widget.RemoteViewsService$RemoteViewsFactoryAdapter.onDataSetChanged 78

    // http://stackoverflow.com/questions/13187284/android-permission-denial-in-widget-remoteviewsfactory-for-content

    // app no longer crashes here, but widget is still blank

//    03-24 11:44:55.863 28236-28236/com.udacity.stockhawk D/WidgetProvider: android.appwidget.action.APPWIDGET_UPDATE_OPTIONS
//03-24 11:44:55.880 28236-28248/com.udacity.stockhawk D/StockRemoteViewsFactory: content://com.udacity.stockhawk/quote
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

        // TODO: Return null if cursor is null, try catch?
        // why is there an invisible widget?
        // why is getViewAt only called for first widget list item???
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

//        03-24 13:56:20.137 27179-27191/com.udacity.stockhawk E/AndroidRuntime: FATAL EXCEPTION: Binder:27179_1
//        Process: com.udacity.stockhawk, PID: 27179
//        java.lang.NullPointerException: Attempt to invoke virtual method 'void android.widget.TextView.setText(java.lang.CharSequence)' on a null object reference
//        at com.udacity.stockhawk.widget.StockRemoteViewsFactory.getViewAt(WidgetRemoteViewsService.java:155)

        // line 155
//        StockSymbolView.setText(symbol);
//        PriceView.setText(dollarFormat.format(price));
//        StockSymbolView.invalidate();
//        PriceView.invalidate();


        // Change remoteView background
        // http://stackoverflow.com/questions/6333774/change-remoteview-imageview-background
        if (rawAbsoluteChange > 0) {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            Timber.d("getViewAt: setBackgroundResource: green");
//            PriceChangeView.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
//            PriceChangeView.setBackgroundResource(R.drawable.percent_change_pill_red);
            Timber.d("getViewAt: setBackgroundResource: red");
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.widget_change, change);
            Timber.d("getViewAt: absolute");
//            PriceChangeView.setText(change);
        } else {
            views.setTextViewText(R.id.widget_change, percentage);
            Timber.d("getViewAt: percentage");
//            PriceChangeView.setText(percentage);
        }

        // views.apply() ????

        // set up Intent and views.setOnClickFillInIntent(); here ????
        // https://developer.android.com/guide/topics/appwidgets/index.html
        // Setting the fill-in Intent
        // Your RemoteViewsFactory must set a fill-in intent on each item in the collection.
        // This makes it possible to distinguish the individual on-click action of a given item.
        // The fill-in intent is then combined with the PendingIntent template in order to determine
        // the final intent that will be executed when the item is clicked.

        // Intent fillInIntent = new Intent();
        // fillInIntent.putExtra("stock", symbol);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        // views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;
    }

    // @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
//            }
//

    // https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#getLoadingView()
    // This allows for the use of a custom loading view which appears between the time that getViewAt(int) is called and returns. If null is returned, a default loading view will be used.
    // If new view returned, then getViewAt is not called.
    // getLoadingView must be implemented
    @Override
    public RemoteViews getLoadingView() {
        Timber.d("getLoadingView");
        // TODO: Fix when loadingView returns a view, getViewAt is never called
        //return new RemoteViews(mContext.getPackageName(), R.id.widget_empty);
        return null;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        Timber.d("getItemId");
        // ??? if (cursor.moveToPosition(position) {
        // return cursor.getLong(Contract.Quote.POSITION_ID);
        // } else
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false; // true ???
    }
}

