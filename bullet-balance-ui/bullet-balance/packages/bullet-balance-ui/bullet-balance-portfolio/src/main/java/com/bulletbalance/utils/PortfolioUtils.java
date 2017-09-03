package com.bulletbalance.utils;

import java.math.BigDecimal;

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

	public static double convertAnnualRateToDaily(double annualRate) {
		return Math.pow(1 + annualRate, 1.0/365) - 1;
	}

	public static double convertDailyRateToAnnual(double dailyRate) {
		return Math.pow(1 + dailyRate, 365) - 1;
	}
}
