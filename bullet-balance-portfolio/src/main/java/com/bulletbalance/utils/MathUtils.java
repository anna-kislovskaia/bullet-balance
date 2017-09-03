package com.bulletbalance.utils;

/**
 * @author berdonosova
 *         Date: 22.12.2016
 */
public class MathUtils {
	public static final int DEFAULT_PRECISION = 6;
	public static final double EPS = Math.pow(10, -MathUtils.DEFAULT_PRECISION);


	public static double round(double value) {
		return round(value, DEFAULT_PRECISION);
	}

	public static double round(double value, int precision) {
		double pow10 = Math.pow(10, precision);
		double normValue = value * pow10;

		if (normValue == 0x1.fffffffffffffp-2) { // greatest double value less than 0.5
			return 0.0;
		}

		return Math.floor(normValue + 0.5d) / pow10;
	}

}
