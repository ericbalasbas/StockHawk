package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udacity.stockhawk.data.Contract.Quote;
import com.udacity.stockhawk.data.Contract.HistoricalQuote;


class DbHelper extends SQLiteOpenHelper {


    private static final String NAME = "StockHawk.db";
    private static final int VERSION = 2;


    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " ("
                + Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Quote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Quote.COLUMN_PRICE + " REAL NOT NULL, "
                + Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, "
                + "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);

        String historical_builder = "CREATE TABLE " + HistoricalQuote.TABLE_NAME + " ("
                + HistoricalQuote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HistoricalQuote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + HistoricalQuote.COLUMN_DATE + " INTEGER NOT NULL, "
                + HistoricalQuote.COLUMN_HIGH + " REAL NOT NULL, "
                + HistoricalQuote.COLUMN_LOW + " REAL NOT NULL, "
                + HistoricalQuote.COLUMN_OPEN + " REAL NOT NULL, "
                + HistoricalQuote.COLUMN_CLOSE + " REAL NOT NULL, "
                + "UNIQUE (" + HistoricalQuote.COLUMN_SYMBOL + ", "
                + HistoricalQuote.COLUMN_DATE
                + " ) ON CONFLICT REPLACE);";

        db.execSQL(historical_builder);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);

        db.execSQL(" DROP TABLE IF EXISTS " + HistoricalQuote.TABLE_NAME);

        onCreate(db);
    }
}
