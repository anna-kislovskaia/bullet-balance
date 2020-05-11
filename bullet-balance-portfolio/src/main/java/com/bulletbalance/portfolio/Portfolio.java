package com.bulletbalance.portfolio;

import com.bulletbalance.asset.AssetProfile;
import com.bulletbalance.utils.MathUtils;
import com.bulletbalance.utils.PortfolioUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Calculates statistic dependencies of assets peeked for portfolio, see {@linkplain #getCorrelationCoefficient(K key1, K key2)}.
 *  Calculates expected risk {@linkplain #calculateAllocationStandardDeviation(BigDecimal[])}
 *  and return {@linkplain #calculateAllocationReturn(BigDecimal[])} for given asset allocation
 */
public class Portfolio<K extends Comparable<K>> {
	private final Map<K, AssetProfile<K>> profiles = new HashMap<>();
	private final List<K> orderedKeys;
	private final HashMap<K, Integer> assetIndexes = new HashMap<>();
	private final int size;
	private double[][] correlationMatrix;
	private Map<AssetKeyPair<K>, PairVariance<K>> pairs = new HashMap<>();

	public Portfolio(Collection<AssetProfile<K>> sources) {
		if (sources.isEmpty()) {
			throw new IllegalArgumentException("Data sources expected");
		}
		size = sources.iterator().next().getCount();
		for (AssetProfile<K> profile: sources) {
			if (size != profile.getCount()) {
				throw new IllegalArgumentException("All assets must have the same size of trade history");
			}
			profiles.put(profile.getKey(), profile);
		}
		List<K> ordered = new ArrayList<>(profiles.keySet());
		Collections.sort(ordered);
		orderedKeys = Collections.unmodifiableList(ordered);
		for(int i = 0; i < orderedKeys.size(); i++) {
			assetIndexes.put(orderedKeys.get(i), i);
		}
	}

	/**
	 * Asset keys in order corresponding to {@linkplain #getCorrelationMatrix()} columns and rows
	 * @return  ordered asset keys
	 */
	public List<K> getAssetKeys() {
		return orderedKeys;
	}

	public AssetProfile<K> getProfile(K key) {
		return profiles.get(key);
	}

	/**
	 * Calculates coeffcient of correlation between given assets on the basis of their price history
	 * @param assetKey1 key of asset 1. Must differ from {@code assetKey2}
	 * @param assetKey2 key of asset 2. Must differ from {@code assetKey1}
	 * @return correlation coefficient
	 * @throws IllegalArgumentException
	 */
	public double getCorrelationCoefficient(K assetKey1, K assetKey2) {
		initCorrelationMatrix();
		AssetKeyPair<K> pairKey = new AssetKeyPair<>(assetKey1, assetKey2);
		PairVariance<K> pairVariance = pairs.get(pairKey);
		if (pairVariance == null) {
			throw new IllegalArgumentException("Unexpected asset keys " + assetKey1 + " " + assetKey2);
		}
		return pairVariance.correlationCoef;
	}

	public double[][] getCorrelationMatrix() {
		initCorrelationMatrix();
		int matrixSize = correlationMatrix.length;
		double[][] copy = new double[matrixSize][];
		for (int i = 0; i < matrixSize; i++) {
			copy[i] = correlationMatrix[i].clone();
		}
		return copy;
	}

	private void initCorrelationMatrix() {
		if (correlationMatrix != null) {
			return ;
		}
		int n = orderedKeys.size();
		double[][] matrix = new double[n][];
		for (int i = 0; i < n; i++) {
			for (int k = 0; k < n; k++) {
				if (k == 0) {
					matrix[i] = new double[n];
				}
				double correlation;
				if (k == i) {
					correlation = 1;
				} else if (i > k) {
					// use already calculated value
					correlation = matrix[k][i];
				} else {
					K assetKey1 = orderedKeys.get(i);
					K assetKey2 = orderedKeys.get(k);
					double covariance = calculateCovariance(assetKey1, assetKey2);

					AssetProfile<K> profile1 = profiles.get(assetKey1);
					AssetProfile<K> profile2 = profiles.get(assetKey2);

					correlation = covariance / (profile1.getStandardDeviation() * profile2.getStandardDeviation());
					correlation = MathUtils.round(correlation);

					PairVariance<K> pair = new PairVariance<>(assetKey1, assetKey2, covariance, correlation);
					pairs.put(pair.pairKey, pair);

				}
				matrix[i][k] = correlation;
			}
		}
		correlationMatrix = matrix;
	}

	private double calculateCovariance(K asset1, K asset2) {
		AssetProfile profile1 = profiles.get(asset1);
		AssetProfile profile2 = profiles.get(asset2);
		double covariance = 0;
		for (int i=0; i < size; i++) {
			covariance += profile1.getDistance(i) * profile2.getDistance(i);
		}
		return covariance;
	}

	/**
	 * Calculates investment return if all wealth is allocated according to given {@code weights}
	 * @param weights allocation coefficients. Sum must be 1
	 * @return expected portfolio return
	 * @throws IllegalArgumentException
	 */
	public double calculateAllocationReturn(@Nonnull BigDecimal[] weights) {
		checkWeights(weights);
		double portfolioReturn = 0;
		int n = profiles.size();
		for(int i = 0; i < n; i++) {
			AssetProfile<K> profile = getProfile(i);
			portfolioReturn += weights[i].doubleValue() * profile.getMeanReturn();
		}
		return portfolioReturn;
	}

	/**
	 * Calculates investment risk if all wealth is allocated according to given {@code weights}
	 * @param weights allocation coefficients. Sum must be 1
	 * @return expected portfolio risk
	 * @throws IllegalArgumentException
	 */
	public double calculateAllocationStandardDeviation(@Nonnull BigDecimal[] weights) {
		checkWeights(weights);
		initCorrelationMatrix();
		double allocationVariance = 0;
		int n = profiles.size();
		for(int i = 0; i < n; i++) {
			AssetProfile<K> profile = getProfile(i);
			allocationVariance += Math.pow(weights[i].doubleValue(), 2) * profile.getVariance();
		}
		for(PairVariance<K> pair : pairs.values()) {
			AssetProfile<K> profile1 = getProfile(pair.getAssetKey1());
			AssetProfile<K> profile2 = getProfile(pair.getAssetKey2());
			double weight1 = weights[assetIndexes.get(pair.getAssetKey1())].doubleValue();
			double weight2 = weights[assetIndexes.get(pair.getAssetKey2())].doubleValue();
			allocationVariance += 2
					* weight1 * weight2
					* profile1.getStandardDeviation() * profile2.getStandardDeviation()
					* pair.correlationCoef;
		}
		double allocationStdDiv = Math.sqrt(allocationVariance);
		return MathUtils.round(allocationStdDiv);
	}

	private void checkWeights(BigDecimal[] weights) {
		if (weights.length != assetIndexes.size()) {
			throw new IllegalArgumentException("Weight for all assets must be provided");
		}
		PortfolioUtils.checkWeightsTotal(weights);
	}

	private AssetProfile<K> getProfile(int index) {
		K assetKey = orderedKeys.get(index);
		return profiles.get(assetKey);
	}

	public int getSize() {
		return size;
	}

	/**
	 * Asset pair identifier
	 */
	private static final class AssetKeyPair<K extends Comparable> {
		private final K key1;
		private final K key2;

		public AssetKeyPair(K key1, K key2) {
			this.key1 = key1;
			this.key2 = key2;

			if (key1 == null || key2 == null) {
				throw new NullPointerException("Null keys are unsupported");
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			AssetKeyPair<?> that = (AssetKeyPair<?>) o;

			if (key1.equals(that.key1)) {
				return key2.equals(that.key2);
			} else if (key1.equals(that.key2)) {
				return key2.equals(that.key1);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int result = key1.hashCode();
			result = 31 * result + key2.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "AssetKeyPair{" +
					key1 + ", " + key2 +
					'}';
		}
	}

	/**
	 * Auxilary container of asset pair data
	 */
	private static final class PairVariance<K extends Comparable<K>> {
		private final AssetKeyPair<K> pairKey;
		private final double covariance;
		private final double correlationCoef;

		public PairVariance(K assetKey1, K assetKey2, double covariance, double correlation) {
			pairKey = new AssetKeyPair<>(assetKey1, assetKey2);
			this.covariance = covariance;
			this.correlationCoef = correlation;
		}

		K getAssetKey1() {
			return pairKey.key1;
		}

		K getAssetKey2() {
			return pairKey.key2;
		}

	}
}
