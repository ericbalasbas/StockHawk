package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();
            ArrayList<ContentValues> historicalQuoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();

                Stock stock = quotes.get(symbol);

                // NOTE: stock does not always equal NULL on missing stocks
                // symbol "RRRREEEWWWWWWW" returns "RRRREEEWWWWWWW: null"
                // "ATT" returns null
                if (stock == null || stock.toString().contains(": null")) {
                    // If stock does not exist remove stock from SharedPreference
                    // Show toast with error message
                    // https://discussions.udacity.com/t/app-crashes-when-i-close-the-app-due-to-localbraodcastmanager/211521/5
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showToast(context);
                        }
                    });

                    // Remove stock from SharedPreferences if it does not exist
                    PrefUtils.removeStock(context, symbol);
                    continue;
                }

                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                // TODO: change to use HISTORICAL_QUOTE table, high, low, open, close
                for (HistoricalQuote it : history) {
                    ContentValues historicalQuoteCV = new ContentValues();
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_SYMBOL, symbol);
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_DATE, it.getDate().getTimeInMillis());
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_HIGH, it.getHigh().floatValue());
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_LOW, it.getLow().floatValue());
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_OPEN, it.getOpen().floatValue());
                    historicalQuoteCV.put(Contract.HistoricalQuote.COLUMN_CLOSE, it.getClose().floatValue());

                    historicalQuoteCVs.add(historicalQuoteCV);
                }

                // TODO: move to after for loop, bulk insert for each symbol
                context.getContentResolver()
                    .bulkInsert(
                            Contract.HistoricalQuote.URI,
                            historicalQuoteCVs.toArray(new ContentValues[historicalQuoteCVs.size()]));

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));



            // TODO: add bulkInsert for HistoricalQuote here

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void showToast(Context context) {
        Toast.makeText(context,"Invalid Stock", Toast.LENGTH_LONG).show();
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");

        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {
        schedulePeriodic(context);
        syncImmediately(context);
    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());

        }
    }
}
