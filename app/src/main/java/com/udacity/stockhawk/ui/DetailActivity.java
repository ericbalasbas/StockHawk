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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
        // Timber.d("initLoader");
    }


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
                        null, null, Contract.HistoricalQuote.HISTORY_SORT_ORDER);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Loader ID:" + Integer.toString(id));
        }

        return returnLoader;

    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO: Handle case where no records found
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
                int count = data.getCount();
                String[] dateArray = new String[count];

                data.moveToFirst();

//                long dateLong = data.getLong(Contract.HistoricalQuote.POSITION_DATE);
//
//                String dateString = Contract.HistoricalQuote.getStringFromDate(dateLong);
//                StockHistoryDate.setText(dateString);
//                StockHistoryDate.invalidate();



                // try finally, cursor.close
                // load stock history into chart data

                List<Entry> entries = new ArrayList<Entry>();

                int i = 0;
                while (data.moveToNext()){

                    long dateLong = data.getLong(Contract.HistoricalQuote.POSITION_DATE);
                    String dateString = Contract.HistoricalQuote.getStringFromDate(dateLong);

                    float closingPrice = data.getFloat(Contract.HistoricalQuote.POSITION_CLOSE);
                    dateArray[i] = dateString;

                    entries.add(new Entry(i, closingPrice));

                    i = i + 1;
                }

//                float[] valuesX = {10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
//                float[] valuesY = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
//                for (int i=0 ; i< valuesX.length ; i++) {
//                    entries.add(new Entry(valuesX[i], valuesY[i]));
//                }

                LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
                LineData lineData = new LineData(dataSet);
                HistoryChart.setData(lineData);
//                YAxis yAxis = HistoryChart.getAxisLeft();
//                yAxis.setAxisMinimum(0f); // start at zero
//                yAxis.setAxisMaximum(100f); // the axis maximum is 100
                XAxis xAxis = HistoryChart.getXAxis();
                xAxis.setValueFormatter(new DateValueFormatter(dateArray));

                HistoryChart.invalidate();

                break;

            default:
                throw new UnsupportedOperationException("Unknown Loader ID:" + Integer.toString(loader.getId()));
        }


        Timber.d("onLoadFinished");

        // load stock history into chart data
        // while(data.hasNext())
        // e = data.next();



    }

    @Override public void onLoaderReset(Loader<Cursor> loader) { }

    @Override protected void onResume() {
        super.onResume();
    }


    // https://discussions.udacity.com/t/mpandroidchart-using-dates-on-x-axis/216615
    public class DateValueFormatter implements IAxisValueFormatter {

        private String[] mValues;

        public DateValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }
    }
}
