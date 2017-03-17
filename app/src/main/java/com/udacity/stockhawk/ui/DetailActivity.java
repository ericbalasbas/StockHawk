package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by eric on 3/9/2017.
 *
 * show 2 years of past weekly price history
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int STOCK_DETAILS_LOADER = 0;
    private static final int STOCK_HISTORY_LOADER = 1;
    private Uri StockQuery;
    // @BindView(R.id.detail_scroll_view) ScrollingView ScrollView;
    // @BindView(R.id.detail_linear_layout) LinearLayout LinearLayoutView;
    @BindView(R.id.stock_symbol) TextView StockSymbol;
    @BindView(R.id.stock_price) TextView StockPrice;
    @BindView(R.id.price_change_absolute) TextView PriceChangeAbsolute;
    @BindView(R.id.price_change_percent) TextView PriceChangePercent;
    @BindView(R.id.stock_history_date) TextView StockHistoryDate;
    @BindView(R.id.stock_open) TextView StockHistoryOpen;
    @BindView(R.id.stock_close) TextView StockHistoryClose;
    @BindView(R.id.stock_high) TextView StockHistoryHigh;
    @BindView(R.id.stock_low) TextView StockHistoryLow;
    @BindView(R.id.history_chart) LineChart HistoryChart;

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        StockQuery = getIntent().getData();

        getSupportLoaderManager().initLoader(STOCK_DETAILS_LOADER, null, this);
        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);


        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        Timber.d(StockQuery.toString());
        // content://com.udacity.stockhawk/quote/FB
        Timber.d("initLoader");
    }

    // ??? how to set min and max axis values?

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");

        Loader<Cursor> returnLoader = null;

        switch (id) {
            case STOCK_DETAILS_LOADER:
                returnLoader = new CursorLoader(this,
                        StockQuery,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, null);
                break;

            case STOCK_HISTORY_LOADER:
                // TODO: change query URI to use HistoricalQuote table
                returnLoader =  new CursorLoader(this,
                        Contract.HistoricalQuote.makeUriForStock(
                                Contract.Quote.getStockFromUri(StockQuery)),
                        Contract.HistoricalQuote.HISTORICAL_QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Loader ID:" + Integer.toString(id));
        }

        return returnLoader;

    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if (data.getCount() != 0) {
//            error.setVisibility(View.GONE);
//        }

        switch (loader.getId()) {
            case STOCK_DETAILS_LOADER:
                data.moveToFirst();

                StockSymbol.setText(data.getString(Contract.Quote.POSITION_SYMBOL));
                StockSymbol.invalidate();

                StockPrice.setText(dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
                StockPrice.invalidate();

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    PriceChangeAbsolute.setBackgroundResource(R.drawable.percent_change_pill_green);
                    PriceChangePercent.setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    PriceChangeAbsolute.setBackgroundResource(R.drawable.percent_change_pill_red);
                    PriceChangePercent.setBackgroundResource(R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                PriceChangeAbsolute.setText(change);
                PriceChangePercent.setText(percentage);

                break;

            case STOCK_HISTORY_LOADER:
                data.moveToFirst();

                StockHistoryDate.setText(data.getString(Contract.HistoricalQuote.POSITION_DATE));
                StockHistoryDate.invalidate();

                List<Entry> entries = new ArrayList<Entry>();
                float[] valuesX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                float[] valuesY = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
                for (int i=0 ; i< valuesX.length ; i++) {
                    entries.add(new Entry(valuesX[i], valuesY[i]));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
                LineData lineData = new LineData(dataSet);
                HistoryChart.setData(lineData);
                HistoryChart.invalidate();

                break;

            default:
                throw new UnsupportedOperationException("Unknown Loader ID:" + Integer.toString(loader.getId()));
        }



        Timber.d("onLoadFinished");

        // load stock history into chart data
        // while(data.hasNext())
        // e = data.next();
//        03-13 18:14:45.334 2490-2490/com.udacity.stockhawk D/DetailActivity: 1488776400000, 138.789993
//        1488171600000, 137.169998
//        1487653200000, 135.440002
//        1486962000000, 133.529999
//        1486357200000, 134.190002

        // java.lang.UnsupportedOperationException: Unknown URI:content://com.udacity.stockhawk/historical_quote%2F*
        // at com.udacity.stockhawk.data.StockProvider.insert(StockProvider.java:128)

        // in QuoteSyncJob.java
//        for (HistoricalQuote it : history) {
//            historyBuilder.append(it.getDate().getTimeInMillis());



    }


    @Override public void onLoaderReset(Loader<Cursor> loader) { }

    @Override protected void onResume() {
        super.onResume();
    }
}
