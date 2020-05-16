package com.bulletbalance.asset;


import com.bulletbalance.utils.MathUtils;

/**
 * Keeps basic statistics data based on price history of a particular asset
 */
public class AssetProfile<K extends Comparable<K>> {
	private final K key;
	private double[] prices;
	private double[] returns;
	private double returnSD;
	private double returnVariance;
	private double meanReturn;
	private double averageReturn;
	private double minPrice;
	private double maxPrice;
	private double lastPrice;

	private AssetProfile(K key) {
		this.key = key;
	}

	public K getKey() {
		return key;
	}

	/**
	 * Calculates basic stat coefficients for given price history. Expected price order is acsending by time:
	 * from older dates to most recent.
	 * Preferable source is closes prices (corrected by dividends for stocks and ETFs)
	 * @param key asset key
	 * @param prices asset price history
	 * @param <T> class must provide implemented {@link  #equals(Object)} and {@linkplain #hashCode()}
	 * @return asset profile
	 */
	public static <T extends Comparable<T>>  AssetProfile<T> create(T key, double[] prices) {
		if (prices == null || prices.length < 2) {
			throw new IllegalArgumentException("Empty price stream");
		}
		AssetProfile<T> profile = new AssetProfile<>(key);
		profile.minPrice = Double.MAX_VALUE;
		profile.maxPrice = -(Double.MAX_VALUE - 1);
		profile.prices = prices;
		profile.returns = new double[prices.length - 1];
		int count = profile.returns.length;

		double previousPrice = Double.NaN;
		double compoundReturn = 1;

		for (int i = 0; i < prices.length; i++) {
			double price = prices[i];
			if (!Double.isFinite(price)) {
				throw new IllegalStateException("Prices must be finite index=" + i + " price=" + price);
			}
			profile.minPrice = Math.min(profile.minPrice, price);
			profile.maxPrice = Math.max(profile.maxPrice, price);
			if (Double.isFinite(previousPrice)) {
				double periodReturn = (price - previousPrice) / previousPrice;
				profile.returns[i - 1] = round(periodReturn);
				profile.averageReturn += periodReturn;
				compoundReturn *= (1 + periodReturn);
			}
			previousPrice = price;
		}

		profile.averageReturn = round(profile.averageReturn / count);


		// SD calculation
		for (double periodReturn : profile.returns) {
			double distance = round(periodReturn - profile.averageReturn);
			profile.returnVariance += Math.pow(distance, 2);
		}
		profile.returnVariance = round(profile.returnVariance);
		profile.returnSD = round(Math.sqrt(profile.returnVariance));

		profile.meanReturn = Math.pow(compoundReturn, 1D/count) - 1;
		profile.lastPrice = previousPrice;
		return profile;
	}

	public double[] getPrices() {
		return prices;
	}

	public double[] getReturns() {
		return returns;
	}

	public double getStandardDeviation() {
		return returnSD;
	}

	public double getVariance() {
		return returnVariance;
	}

	public double getMinPrice() {
		return minPrice;
	}

	public double getMaxPrice() {
		return maxPrice;
	}

	public double getLastPrice() {
		return prices[prices.length - 1];
	}

	/**
	 * Implied rate of investing to asset during given period
	 * @return implied rate in fractions
	 */
	public double getMeanReturn() {
		return meanReturn;
	}

	public double getPriceRange() {
		double range = (lastPrice - minPrice) / (maxPrice - minPrice);
		return round(range);
	}

	public int getCount() {
		return returns.length;
	}

	public double getAverageReturn() {
		return averageReturn;
	}

	public double getDistance(int i) {
		if (i >=0 && i < getCount()) {
			return round(returns[i] - averageReturn);
		}
		throw new IndexOutOfBoundsException();
	}

	public static double round(double value) {
		return MathUtils.round(value);
	}

}
