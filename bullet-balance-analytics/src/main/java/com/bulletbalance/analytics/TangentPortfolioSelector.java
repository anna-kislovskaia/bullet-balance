package com.bulletbalance.analytics;

import com.bulletbalance.utils.MathUtils;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

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
		int size = results.size();
		List<AllocationResult> increasingReturns = new ArrayList<>(size);
		AllocationResult previous = null;
		int aboveRiskFreeRateIndex = -1;
		for (AllocationResult current : results) {
			if (previous == null || previous.getWeightedReturn() + MathUtils.EPS < current.getWeightedReturn()) {
				previous = current;
				increasingReturns.add(current);

				if (aboveRiskFreeRateIndex < 0 && current.getWeightedReturn() > riskFreeRate) {
					aboveRiskFreeRateIndex = increasingReturns.size() - 1;
				}
			}
		}

		if (aboveRiskFreeRateIndex < 0) {
			System.out.println("No returns above risk free rate were found");
			return null;
		}

		for (int i = aboveRiskFreeRateIndex, n = increasingReturns.size(); i < n; i++) {
			AllocationResult current = increasingReturns.get(i);
			if (i + 1 < n) {
				// check possible intersection
				AllocationResult next = increasingReturns.get(i + 1);
				double slope = calculateSlope(current);
				double y = slope * next.getWeigthedRisk() + riskFreeRate;
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
		return y / result.getWeigthedRisk();
	}

}
