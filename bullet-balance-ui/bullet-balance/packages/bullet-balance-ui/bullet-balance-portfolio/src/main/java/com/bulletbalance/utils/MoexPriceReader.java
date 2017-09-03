package com.bulletbalance.utils;

import java.io.LineNumberReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author berdonosova
 *         Date: 22.12.2016
 */
public class MoexPriceReader {

	private static final int LAST_PRICE_INDEX = 8;
	private static final int AVG_PRICE_INDEX = 4;

	public static double[] readLastPrices(Reader reader) {
		return readPrices(reader, LAST_PRICE_INDEX);
	}

	public static double[] readAvgPrices(Reader reader) {
		return readPrices(reader, AVG_PRICE_INDEX);
	}

	static String prepareValue(String value) {
		return value.replaceAll("\\s", "");
	}

	static DecimalFormat createMoexPriceFormat() {
		DecimalFormat format = new DecimalFormat("#,###,###.#######");
		format.getDecimalFormatSymbols().setGroupingSeparator(' ');
		format.getDecimalFormatSymbols().setDecimalSeparator(',');
		return format;
	}

	private static double[] readPrices(Reader reader, int index) {
		DecimalFormat format = createMoexPriceFormat();

		LineNumberReader lineReader = new LineNumberReader(reader);
		List<Double> pricesValues = lineReader.lines().map(line -> {
			String[] values = line.split("\t");
			if (values.length <= index) {
				throw new IllegalArgumentException("Incorrect line format: " + lineReader.getLineNumber());
			}
			try {
				Number number = format.parse(prepareValue(values[index]));
				return number.doubleValue();
			} catch (ParseException e) {
				throw new IllegalArgumentException("Incorrect value format: " + lineReader.getLineNumber());
			}
		}).collect(Collectors.toList());
		double[] prices = new double[pricesValues.size()];
		for (int i = 0, n = pricesValues.size(); i < n; i++) {
			prices[i] = pricesValues.get(i);
		}
		return prices;
	};


}
