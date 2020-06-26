package com.bulletbalance.utils;

import com.bulletbalance.portfolio.AggregationPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author berdonosova
 *         Date: 01.09.2017
 */
public class PortfolioUtils {

	public static void checkWeightsTotal(BigDecimal[] weights) {
		BigDecimal totalWeight = BigDecimal.ZERO;
		for (BigDecimal weight : weights) {
			if (weight == null) {
				throw new IllegalArgumentException("All weights must be defined");
			}
			totalWeight = totalWeight.add(weight);
		}
		if (totalWeight.doubleValue() != 1) {
			throw new IllegalArgumentException("All weights must be defined");
		}
	}

	public static double convertAnnualRateToDaily(double annualRate, AggregationPeriod aggregationPeriod) {
		return Math.pow(1 + annualRate, 1.0/aggregationPeriod.getPeriodsInYear()) - 1;
	}

	public static double convertDailyRateToAnnual(double dailyRate, AggregationPeriod aggregationPeriod) {
		return Math.pow(1 + dailyRate, aggregationPeriod.getPeriodsInYear()) - 1;
	}

	public static double convertDailyRiskToAnnual(double risk, AggregationPeriod aggregationPeriod) {
		return risk * Math.sqrt(aggregationPeriod.getPeriodsInYear());
	}

	public static LocalDate intToLocalDate(int date) {
		int year = date / 10000;
		int month = (date % 10000) / 100;
		int day = date % 100;
		return LocalDate.of(year, month, day);
	}
}
