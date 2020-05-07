package com.bulletbalance.random;

import com.bulletbalance.utils.PortfolioUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks consistency of weights allocation
 */
public class RandomWeightsTest {

	private RandomWeightsGenerator weightsGenerator;

	@Before
	public void init() {
		weightsGenerator = new NoShortSellWeightsGenerator();
	}

	@Test
	public void testConsistency() {
		for (int i = 1; i < 1_000_000; i = i*10) {
			BigDecimal[] weights = weightsGenerator.generateWeights(i);
			PortfolioUtils.checkWeightsTotal(weights);
		}
	}

	@Test
	public void testDistribution() {
		Set<BigDecimal[]> probes = new HashSet<>();
		for (int i = 0; i < 10000; i++) {
			BigDecimal[] weights = weightsGenerator.generateWeights(2);
			Assert.assertTrue(probes.add(weights));
		}
		System.out.println("");
	}

	@Test
	public void testAssignmentCount() {
		// check whether each asset will participate in at least one calculation
		for(int i = 1; i < 100; i+=5) {
			testAssignmentCount(i);
		}
	}

	private void testAssignmentCount(int assetCount) {
		int[] assignments = new int[assetCount];
		for (int i = 1; i < assetCount * 1_000; i++) {
			BigDecimal[] weights = weightsGenerator.generateWeights(assetCount);
			PortfolioUtils.checkWeightsTotal(weights);
			for (int k = 0; k < assetCount; k++) {
				if (weights[k].doubleValue() != 0) {
					assignments[k]++;
				}
			}
		}

		System.out.println(Arrays.toString(assignments));
		for (int k = 0; k < assetCount; k++) {
			Assert.assertTrue(assignments[k] > 0);
		}

	}
}
