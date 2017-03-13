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
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

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
    private Uri StockQuery;
    // @BindView(R.id.detail_scroll_view) ScrollingView ScrollView;
    @BindView(R.id.stock_symbol) TextView StockSymbol;
    @BindView(R.id.stock_price) TextView StockPrice;
    @BindView(R.id.stock_history) TextView StockHistory;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        StockQuery = getIntent().getData();

        getSupportLoaderManager().initLoader(STOCK_DETAILS_LOADER, null, this);
        Timber.d(StockQuery.toString());
        Timber.d("initLoader");
    }

    // ??? how to set min and max axis values?

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        return new CursorLoader(this,
                StockQuery,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.HISTORY_SORT_ORDER);

    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if (data.getCount() != 0) {
//            error.setVisibility(View.GONE);
//        }

        data.moveToFirst();

        Timber.d("onLoadFinished");

        // get stock symbol
        StockSymbol.setText(data.getString(Contract.Quote.POSITION_SYMBOL));
        StockSymbol.invalidate();

        // get current price
        StockPrice.setText(data.getString(Contract.Quote.POSITION_PRICE));
        StockPrice.invalidate();

        // get stock history
        StockHistory.setText(data.getString(Contract.Quote.POSITION_HISTORY));
        StockHistory.invalidate();

    }


    @Override public void onLoaderReset(Loader<Cursor> loader) {
        // swipeRefreshLayout.setRefreshing(false);
        // StockHistory.close();
    }

    @Override protected void onResume() {
        super.onResume();
    }
}
