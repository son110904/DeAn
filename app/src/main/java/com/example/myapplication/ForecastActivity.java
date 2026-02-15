package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastActivity extends AppCompatActivity {

    TextView tvForecast;
    TextView tvTrend;
    TextView tvForecastAdvice;
    LineChart lineChartForecast;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        tvForecast = findViewById(R.id.tvForecast);
        tvTrend = findViewById(R.id.tvTrend);
        tvForecastAdvice = findViewById(R.id.tvForecastAdvice);
        lineChartForecast = findViewById(R.id.lineChartForecast);
        bottomNav = findViewById(R.id.bottomNav);

        setupChart();
        showEmptyState();

        bottomNav.setSelectedItemId(R.id.menu_forecast);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_forecast) {
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchForecast();
    }

    private void fetchForecast() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showEmptyState();
                    return;
                }

                List<Integer> history = buildExpenseHistory(response.body());
                if (history.isEmpty()) {
                    showEmptyState();
                    return;
                }

                requestForecast(history);
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                showEmptyState();
            }
        });
    }

    private void requestForecast(List<Integer> history) {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getForecast(new ForecastRequest(history, 3)).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderForecast(response.body());
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                showEmptyState();
            }
        });
    }

    private List<Integer> buildExpenseHistory(List<TransactionResponse> transactions) {
        List<TransactionResponse> sorted = new ArrayList<>(transactions);
        Collections.reverse(sorted);

        List<Integer> expenseHistory = new ArrayList<>();
        for (TransactionResponse transaction : sorted) {
            if (!"expense".equalsIgnoreCase(transaction.getType())) {
                continue;
            }
            expenseHistory.add(transaction.getAmount());
        }
        return expenseHistory;
    }

    private void renderForecast(ForecastResponse forecast) {
        float predictedValue = forecast.getPredictedValue();
        tvForecast.setText(TransactionStore.formatCurrency(Math.round(predictedValue)));

        String trendText;
        int trendColor;
        switch (forecast.getTrend()) {
            case "up":
                trendText = getString(R.string.forecast_trend_up);
                trendColor = ContextCompat.getColor(this, R.color.accent_red);
                break;
            case "down":
                trendText = getString(R.string.forecast_trend_down);
                trendColor = ContextCompat.getColor(this, R.color.accent_green);
                break;
            default:
                trendText = getString(R.string.forecast_trend_stable);
                trendColor = ContextCompat.getColor(this, R.color.text_secondary);
                break;
        }

        tvTrend.setText(trendText);
        tvTrend.setTextColor(trendColor);

        if (predictedValue > 2_000_000f) {
            tvForecastAdvice.setText(getString(R.string.forecast_advice_high));
        } else if (predictedValue >= 1_000_000f) {
            tvForecastAdvice.setText(getString(R.string.forecast_advice_enough));
        } else {
            tvForecastAdvice.setText(getString(R.string.forecast_advice_low));
        }

        renderForecastChart(forecast.getForecastSeries());
    }

    private void showEmptyState() {
        tvForecast.setText(getString(R.string.forecast_empty));
        tvTrend.setText(getString(R.string.forecast_trend_stable));
        tvTrend.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvForecastAdvice.setText(getString(R.string.forecast_advice_empty));
        lineChartForecast.clear();
    }

    private void setupChart() {
        lineChartForecast.getDescription().setEnabled(false);
        lineChartForecast.getLegend().setEnabled(false);
        lineChartForecast.setNoDataText(getString(R.string.chart_no_data));
        lineChartForecast.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChartForecast.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void renderForecastChart(List<Float> forecastSeries) {
        if (forecastSeries.isEmpty()) {
            lineChartForecast.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int index = 0; index < forecastSeries.size(); index++) {
            entries.add(new Entry(index, forecastSeries.get(index)));
            labels.add(getString(R.string.forecast_month_label, index + 1));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, getString(R.string.forecast_trend_title));
        lineDataSet.setColor(ContextCompat.getColor(this, R.color.primary_blue));
        lineDataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary_blue));
        lineDataSet.setLineWidth(2.2f);
        lineDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        XAxis xAxis = lineChartForecast.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        lineChartForecast.setData(new LineData(lineDataSet));
        lineChartForecast.invalidate();
    }
}
