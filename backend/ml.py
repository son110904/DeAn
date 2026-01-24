from typing import List


def forecast_expenses(history: List[int]) -> float:
    if not history:
        return 0.0
    return sum(history) / len(history)
