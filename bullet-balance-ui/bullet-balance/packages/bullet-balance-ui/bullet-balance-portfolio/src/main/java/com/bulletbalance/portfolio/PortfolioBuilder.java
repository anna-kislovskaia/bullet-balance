package com.bulletbalance.portfolio;

import com.bulletbalance.asset.AssetProfile;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds  portfolio on the basis of asset price hisotries.
 * Size of price history of all assets expected to be the same.
 * Missed values can be filled up with {@linkplain Double#NaN}
 * Points corresponding to missed data will be excluded from calculation for <b>all<b/> assets in portfolio
 */
public class PortfolioBuilder<K extends Comparable<K>> {
	private Map<K, double[]> prices = new HashMap<>();
	private int count;

	/**
	 * Registers price history
	 * Size of price history of all assets expected to be the same.
	 * Missed values can be filled up with {@linkplain Double#NaN}
	 * Points corresponding to missed data will be excluded from calculation for <b>all<b/> assets in portfolio
	 * @param assetKey  unique asset identifier
	 * @param assetPrices asset price history
	 */
	public void add(@Nonnull K assetKey, @Nonnull double[] assetPrices) {
		if (count == 0) {
			count = assetPrices.length;
		} else if (assetPrices.length != count) {
			throw new IllegalArgumentException("Prices must have equal size");
		}
		prices.put(assetKey, assetPrices);
	}

	public Portfolio<K> build() {
		Collection<Integer> invalidIndexes = new HashSet<>();
		for (double[] assetPrices : prices.values()) {
			for (int i = 0; i < assetPrices.length; i++) {
				double price = assetPrices[i];
				if (!Double.isFinite(price)) {
					invalidIndexes.add(i);
				}
			}
		}
		Collection<AssetProfile<K>> profiles = prices.keySet().stream()
				.map(assetKey -> {
					double[] reduced = reducePriceHistory(prices.get(assetKey), invalidIndexes);
					return AssetProfile.create(assetKey, reduced);
				})
				.collect(Collectors.toList());
		return new Portfolio<>(profiles);
	}

	private static double[] reducePriceHistory(double[] rawPrices, Collection<Integer> invalidIndexes) {
		if (invalidIndexes.isEmpty()) {
			return rawPrices;
		}
		double[] reduced = new double[rawPrices.length - invalidIndexes.size()];
		for (int i = 0, k = 0; i < rawPrices.length; i++) {
			if (!invalidIndexes.contains(i)) {
				reduced[k] = rawPrices[i];
				k++;
			}
		}
		return reduced;
	}
}
