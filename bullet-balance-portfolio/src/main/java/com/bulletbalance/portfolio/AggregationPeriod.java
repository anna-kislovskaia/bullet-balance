package com.bulletbalance.portfolio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum AggregationPeriod {
    DAY(365),
    WEEK(52),
    MONTH(12);

    private final int periodsInYear;

    AggregationPeriod(int periodsInYear) {
        this.periodsInYear = periodsInYear;
    }

    public int getPeriodsInYear() {
        return periodsInYear;
    }

    public LocalDate getNextDate(LocalDate initial) {
        switch (this) {
            case DAY: return initial.plusDays(1);
            case WEEK: {
                LocalDate endOfWeek = initial.with(DayOfWeek.FRIDAY);
                if (endOfWeek.isBefore(initial) || endOfWeek.isEqual(initial)) {
                    return endOfWeek.plusWeeks(1);
                }
                return endOfWeek;
            }
            case MONTH: {
                LocalDate endOfMonth = initial.withDayOfMonth(1).plusMonths(1).minusDays(1);
                if (endOfMonth.isBefore(initial) || endOfMonth.isEqual(initial)) {
                    return initial.withDayOfMonth(1).plusMonths(2).minusDays(1);
                }
                return endOfMonth;
            }
            default:
                throw new IllegalArgumentException("Unsupported aggregation period " + this);
        }
    }

    public static AggregationPeriod getPeriod(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end.plusDays(1));
        if (days < 90) {
            return DAY;
        } else if (days < 365) {
            return WEEK;
        } else {
            return MONTH;
        }
    }
}
