package com.bulletbalance.analytics;

import com.bulletbalance.utils.MathUtils;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Selects point on hyperbola which also lays on straight line having {0, {@linkplain #riskFreeRate}} as one of points
 */
public class TangentPortfolioSelector implements AllocationResultSelector {

	private final double riskFreeRate;

	public TangentPortfolioSelector(double riskFreeRate) {
		this.riskFreeRate = riskFreeRate;
	}

	@Override
	@Nullable
	public AllocationResult selectResult(List<AllocationResult> results) {
		System.out.println(String.format("Risk free rate %f9", riskFreeRate));
		List<AllocationResult> increasingReturns = filterSamples(results, RETURN_COMPARATOR);
		for (int i = 0, n = increasingReturns.size(); i < n; i++) {
			AllocationResult current = increasingReturns.get(i);
			if (current.getWeightedReturn() >= riskFreeRate && i + 1 < n) {
				// check possible intersection
				AllocationResult next = increasingReturns.get(i + 1);
				double slope = calculateSlope(current);
				double y = slope * next.getWeighthedRisk() + riskFreeRate;
				if (y > next.getWeightedReturn()) {
					return current;
				}
			}

		}
		return null;
	}

	/**
	 * y = slope * x + {@linkplain #riskFreeRate}
	 * @param result allocation with return above risk free rate
	 * @return slope
	 */
	private double calculateSlope(AllocationResult result) {
		double y = result.getWeightedReturn() - riskFreeRate;
		return y / result.getWeighthedRisk();
	}

	public static final Comparator<AllocationResult> RETURN_COMPARATOR = (sample1, sample2) -> Double.compare(sample1.getWeightedReturn(), sample2.getWeightedReturn() + MathUtils.EPS);

	public static List<AllocationResult> filterSamples(List<AllocationResult> samples, Comparator<AllocationResult> comparator) {
		List<AllocationResult> sortedReturns = new ArrayList<>();
		AllocationResult previous = null;
		for (AllocationResult current : samples) {
			if (previous == null || comparator.compare(current, previous) > 0) {
				previous = current;
				sortedReturns.add(current);
			}
		}
		return sortedReturns;
	}

}
