package com.udacity.stockhawk.data;


import android.net.Uri;
import android.provider.BaseColumns;

import com.google.common.collect.ImmutableList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Contract {

    static final String AUTHORITY = "com.udacity.stockhawk";
    static final String PATH_QUOTE = "quote";
    static final String PATH_QUOTE_WITH_SYMBOL = "quote/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_HISTORICAL_QUOTE = "historical_quote";

    private Contract() {
    }

    @SuppressWarnings("unused")
    public static final class Quote implements BaseColumns {

        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build();
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_ABSOLUTE_CHANGE = "absolute_change";
        public static final String COLUMN_PERCENTAGE_CHANGE = "percentage_change";
        public static final int POSITION_ID = 0;
        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_PRICE = 2;
        public static final int POSITION_ABSOLUTE_CHANGE = 3;
        public static final int POSITION_PERCENTAGE_CHANGE = 4;
        public static final ImmutableList<String> QUOTE_COLUMNS = ImmutableList.of(
                _ID,
                COLUMN_SYMBOL,
                COLUMN_PRICE,
                COLUMN_ABSOLUTE_CHANGE,
                COLUMN_PERCENTAGE_CHANGE
        );
        static final String TABLE_NAME = "quotes";

        public static Uri makeUriForStock(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        public static String getStockFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }


    }

    @SuppressWarnings("unused")
    public static final class HistoricalQuote implements BaseColumns {
        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_HISTORICAL_QUOTE).build();
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_HIGH = "high";
        public static final String COLUMN_LOW = "low";
        public static final String COLUMN_OPEN = "open";
        public static final String COLUMN_CLOSE = "close";
        public static final int POSITION_ID = 0;
        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_DATE = 2;
        public static final int POSITION_HIGH = 3;
        public static final int POSITION_LOW = 4;
        public static final int POSITION_OPEN = 5;
        public static final int POSITION_CLOSE = 6;
        public static final ImmutableList<String> HISTORICAL_QUOTE_COLUMNS = ImmutableList.of(
                _ID,
                COLUMN_SYMBOL,
                COLUMN_DATE,
                COLUMN_HIGH,
                COLUMN_LOW,
                COLUMN_OPEN,
                COLUMN_CLOSE
        );

        // TODO: sort order for details view
        public static final String HISTORY_SORT_ORDER = COLUMN_DATE + " ASC";

        static final String TABLE_NAME = "historical_quotes";

        public static Uri makeUriForStock(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        static String getStockFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }

        // TODO: Document date column, and how it is stored/transformed
        // TODO: Place date utility functions here? Date long to string

        // http://stackoverflow.com/questions/7487460/java-convert-long-to-date
        // Date d = new Date(TimeUnit.SECONDS.toMillis(1220227200L));

        // https://discussions.udacity.com/t/how-to-store-only-date-in-database/216360/7
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // String date = dateFormat.format(it.getDate().getTime());

        // in QuoteSyncJob.java
//        for (HistoricalQuote it : history) {
//            historyBuilder.append(it.getDate().getTimeInMillis());

        public static String getStringFromDate(long dateLong) {
            Date d = new Date(dateLong);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return dateFormat.format(d);
        }
    }

}
