package com.bulletbalance.random;


import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Generates random allocations of assets
 * Sum of weights is always equal to 1
 */
public interface RandomWeightsGenerator {

	@Nonnull
	BigDecimal[] generateWeights(int count);
}
