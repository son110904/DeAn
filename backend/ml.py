from dataclasses import dataclass
from typing import List


@dataclass
class ForecastResult:
    predicted_value: float
    trend: str
    forecast_series: list[float]


def forecast_expenses(history: List[int]) -> float:
    if not history:
        return 0.0
    return sum(history) / len(history)


def forecast_expenses_sarima(history: List[int], steps: int = 3) -> ForecastResult:
    if not history:
        return ForecastResult(predicted_value=0.0, trend="stable", forecast_series=[])

    normalized_steps = max(1, min(12, steps))
    clean_history = [float(value) for value in history if value >= 0]
    if not clean_history:
        return ForecastResult(predicted_value=0.0, trend="stable", forecast_series=[])

    try:
        from statsmodels.tsa.statespace.sarimax import SARIMAX

        season_length = 12 if len(clean_history) >= 12 else max(2, len(clean_history) // 2)
        model = SARIMAX(
            clean_history,
            order=(1, 1, 1),
            seasonal_order=(1, 0, 1, season_length),
            enforce_stationarity=False,
            enforce_invertibility=False,
        )
        fitted = model.fit(disp=False)
        forecast_values = fitted.forecast(steps=normalized_steps)
        forecast_series = [max(0.0, float(value)) for value in forecast_values]
    except Exception:
        # Fallback để đảm bảo endpoint luôn phản hồi ngay cả khi thiếu statsmodels
        last_value = clean_history[-1]
        avg_delta = 0.0
        if len(clean_history) > 1:
            deltas = [clean_history[idx] - clean_history[idx - 1] for idx in range(1, len(clean_history))]
            avg_delta = sum(deltas) / len(deltas)
        forecast_series = [max(0.0, last_value + avg_delta * (step + 1)) for step in range(normalized_steps)]

    predicted_value = forecast_series[0] if forecast_series else clean_history[-1]
    baseline = clean_history[-1]
    delta = predicted_value - baseline
    threshold = max(1.0, baseline * 0.01)

    if delta > threshold:
        trend = "up"
    elif delta < -threshold:
        trend = "down"
    else:
        trend = "stable"

    return ForecastResult(
        predicted_value=predicted_value,
        trend=trend,
        forecast_series=forecast_series,
    )
