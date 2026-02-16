package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class ForecastResponse {
    @SerializedName("predicted_value")
    private float predictedValue;
    private String trend;
    @SerializedName("forecast_series")
    private List<Float> forecastSeries;

    public float getPredictedValue() {
        return predictedValue;
    }

    public String getTrend() {
        return trend == null ? "stable" : trend;
    }

    public List<Float> getForecastSeries() {
        return forecastSeries == null ? Collections.emptyList() : forecastSeries;
    }
}
