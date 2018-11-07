package com.bulletbalance.random;

import com.bulletbalance.utils.MathUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates non-negative weights of all assets
 */
public class NoShortSellWeightsGenerator implements RandomWeightsGenerator {
	private static final BigDecimal TOTAL = BigDecimal.ONE.setScale(MathUtils.DEFAULT_PRECISION, RoundingMode.HALF_UP);

	@Nonnull
	public BigDecimal[] generateWeights(int count) {
		if (count == 1) {
			return new BigDecimal[]{TOTAL};
		}
		double[] randomWeights = new double[count];
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double totalWeight = 0;
		for (int i = 0; i < count; i++) {
			randomWeights[i] = random.nextInt(101);
			totalWeight += randomWeights[i];
		}

		BigDecimal[] weights = new BigDecimal[count];
		BigDecimal restOfTotal = TOTAL;
		for (int i = 0; i < count; i++) {
			BigDecimal weight = new BigDecimal(randomWeights[i] / totalWeight)
					.setScale(MathUtils.DEFAULT_PRECISION, RoundingMode.HALF_UP);
			if (restOfTotal.compareTo(weight) > 0 && i < count - 1) {
                weights[i] = weight;
                restOfTotal = restOfTotal.subtract(weight);
			} else if (restOfTotal.compareTo(BigDecimal.ZERO) > 0) {
				weights[i] = restOfTotal;
				restOfTotal = BigDecimal.ZERO;
			} else {
				weights[i] = BigDecimal.ZERO;
			}
		}
		return weights;
 }

}
