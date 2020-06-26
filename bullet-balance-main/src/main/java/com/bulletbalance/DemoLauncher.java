package com.bulletbalance;

import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.analytics.AllocationResultSelector;
import com.bulletbalance.analytics.LeastRiskyAllocationSelector;
import com.bulletbalance.analytics.TangentPortfolioSelector;
import com.bulletbalance.portfolio.AggregationPeriod;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.random.NoShortSellWeightsGenerator;
import com.bulletbalance.utils.PortfolioUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Console launcher
 */
public class DemoLauncher {

	// to be injected
	private static final Map<String, AllocationResultSelector> processors = new LinkedHashMap<>();
	static {
		processors.put("Least risky", new LeastRiskyAllocationSelector());
		processors.put("Markowitz tangent portfolio", new TangentPortfolioSelector(PortfolioUtils.convertAnnualRateToDaily(0.05, AggregationPeriod.DAY)));
	}

	public static void main(String[] args) {
		AllocationSamplesGenerator analyzer = new AllocationSamplesGenerator();
		analyzer.setWeightsGenerator(new NoShortSellWeightsGenerator());
		final Portfolio<String> portfolio = MoexDemo.createPortfolio();
		analyzer.setPortfolio(portfolio);
		List<AllocationResult> results = analyzer.generate();

		for (String name : processors.keySet()) {
			AllocationResultSelector processor = processors.get(name);
			AllocationResult result = processor.selectResult(results);
			printSelectorChoice(name, portfolio, result);
		}
	}

	private static void printSelectorChoice(String dislayName, Portfolio portfolio, AllocationResult result) {
		if (result == null) {
			System.out.println(String.format("%s produced no results", dislayName));
		} else {
			double annualRate = PortfolioUtils.convertDailyRateToAnnual(result.getWeightedReturn(), AggregationPeriod.DAY);

			System.out.println(String.format("%s allocation is %s, annual return %f", dislayName, result.toString(), annualRate));
			System.out.println(portfolio.getAssetKeys());
			System.out.println(Arrays.toString(result.getWeights()));
		}
	}
}

