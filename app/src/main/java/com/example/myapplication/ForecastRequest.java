package com.example.myapplication;

import java.util.List;

public class ForecastRequest {
    private final List<Integer> history;
    private final int steps;

    public ForecastRequest(List<Integer> history, int steps) {
        this.history = history;
        this.steps = steps;
    }

    public List<Integer> getHistory() {
        return history;
    }

    public int getSteps() {
        return steps;
    }
}
