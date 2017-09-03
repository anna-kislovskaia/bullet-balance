package com.bulletbalance.analytics;

import com.bulletbalance.utils.MathUtils;

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
	public AllocationResult selectResult(List<AllocationResult> results) {
		System.out.println(String.format("Risk free rate %f9", riskFreeRate));
		int size = results.size();
		List<AllocationResult> increasingReturns = new ArrayList<>(size);
		AllocationResult previous = null;
		int aboveRiskFreeRateIndex = -1;
		for (AllocationResult current : results) {
			if (previous == null || previous.getAllocationReturn() + MathUtils.EPS < current.getAllocationReturn()) {
				previous = current;
				increasingReturns.add(current);

				if (aboveRiskFreeRateIndex < 0 && current.getAllocationReturn() > riskFreeRate) {
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
				double y = slope * next.getAllocationStd() + riskFreeRate;
				if (y > next.getAllocationReturn()) {
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
		double y = result.getAllocationReturn() - riskFreeRate;
		return y / result.getAllocationStd();
	}

}
