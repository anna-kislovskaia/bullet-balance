package com.bulletbalance.random;

import com.bulletbalance.utils.MathUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates non-negative weights of all assets
 */
public class NoShortSellWeightsGenerator implements RandomWeightsGenerator {
	private static final BigDecimal TOTAL = BigDecimal.ONE.setScale(MathUtils.DEFAULT_PRECISION, RoundingMode.HALF_UP);
	private static final Random rnd = new Random();

	@Nonnull
	public BigDecimal[] generateWeights(int count) {
		if (count == 1) {
			return new BigDecimal[]{TOTAL};
		}
		double[] randomWeights = new double[count];
		double totalWeight = 0;
		for (int i = 0; i < count; i++) {
			randomWeights[i] = Math.abs(rnd.nextInt());
			totalWeight += randomWeights[i];
		}

		ArrayList<BigDecimal> weights = new ArrayList<>(count);
		BigDecimal restOfTotal = TOTAL;
		for (int i = 0; i < count; i++) {
			BigDecimal weight = new BigDecimal(randomWeights[i] / totalWeight)
					.setScale(MathUtils.DEFAULT_PRECISION, RoundingMode.HALF_UP);
			if (restOfTotal.compareTo(weight) > 0 && i < count - 1) {
                weights.add(weight);
                restOfTotal = restOfTotal.subtract(weight);
			} else if (restOfTotal.compareTo(BigDecimal.ZERO) > 0) {
				weights.add(restOfTotal);
				restOfTotal = BigDecimal.ZERO;
			} else {
				weights.add(BigDecimal.ZERO);
			}
		}
		Collections.shuffle(weights);
		return weights.toArray(new BigDecimal[count]);
 }

}
