package com.bulletbalance.utils;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author berdonosova
 *         Date: 01.09.2017
 */
public class PortfolioUtils {

	private static final int DAYS_IN_YEAR = 365;

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

	public static double convertAnnualRateToDaily(double annualRate) {
		return Math.pow(1 + annualRate, 1.0/DAYS_IN_YEAR) - 1;
	}

	public static double convertDailyRateToAnnual(double dailyRate) {
		return Math.pow(1 + dailyRate, DAYS_IN_YEAR) - 1;
	}

	public static double convertDailyRiskToAnnual(double risk) {
		return risk * Math.sqrt(DAYS_IN_YEAR);
	}

	public static LocalDate intToLocalDate(int date) {
		int year = date / 10000;
		int month = (date % 10000) / 100;
		int day = date % 100;
		return LocalDate.of(year, month, day);
	}
}
