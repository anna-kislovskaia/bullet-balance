package com.bulletbalance;

import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.utils.MathUtils;
import com.bulletbalance.random.RandomWeightsGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Generates allocation samples for further analytics
 * Use {@linkplain #setSamplesCount(int)} to set up desired number of samples.<br/>
 * Not thread-safe
 */
public class AllocationSamplesGenerator {
	private int samplesCount = 100_000;
	private RandomWeightsGenerator weightsGenerator;
	private Portfolio portfolio;

	public void setSamplesCount(int samplesCount) {
		if (samplesCount <= 0) {
			throw new IllegalArgumentException("Number of samples must be positive");
		}
		this.samplesCount = samplesCount;
	}

	public void setWeightsGenerator(RandomWeightsGenerator weightsGenerator) {
		if (weightsGenerator == null) {
			throw new IllegalArgumentException("Weights producer must be defined");
		}
		this.weightsGenerator = weightsGenerator;
	}

	public void setPortfolio(Portfolio portfolio) {
		if (portfolio == null) {
			throw new IllegalArgumentException("Portfolio must be defined");
		}
		this.portfolio = portfolio;
	}

	/**
	 * Generates desired number of allocation samples for given portfolio
	 * <b>NOTE<b/> blocks invokation thread until all samples are generated
	 * @return allocation samples
	 */
	public List<AllocationResult> generate() {
		if (portfolio == null) {
			throw new IllegalStateException("Portfolio must be provided");
		}
		System.out.println(String.format("Starting generation of asset allocations: number of sample is %d ...", samplesCount));
		int size = portfolio.getAssetKeys().size();
		List<Callable<AllocationResult>> tasks = new ArrayList<>(samplesCount);

		for (int i = 0; i < samplesCount; i++) {
			tasks.add(() -> {
				BigDecimal[] weights = weightsGenerator.generateWeights(size);
				double allocationReturn = MathUtils.round(portfolio.calculateAllocationReturn(weights));
				double allocationStd = MathUtils.round(portfolio.calculateAllocationStandardDeviation(weights));
				return new AllocationResult(weights, allocationReturn, allocationStd);
			});
		}

		ForkJoinPool pool = new ForkJoinPool();
		List<Future<AllocationResult>> futures = pool.invokeAll(tasks);
		// wait for results
		List<AllocationResult> results = futures.stream()
				.map(future -> {
					try {
						return future.get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		System.out.println(String.format("%d allocations are generated", samplesCount));
		results.sort(AllocationResult.RISK_COMPARATOR);
		return results;
	}

}
