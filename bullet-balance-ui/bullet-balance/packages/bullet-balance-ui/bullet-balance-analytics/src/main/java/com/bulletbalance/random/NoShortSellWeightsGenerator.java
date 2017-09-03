package com.bulletbalance.random;

import com.bulletbalance.utils.MathUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates non-negative weights of all assets
 */
public class NoShortSellWeightsGenerator implements RandomWeightsGenerator {

	@Nonnull
	public BigDecimal[] generateWeights(int count) {
	 if (count == 1) {
		 return new BigDecimal[]{BigDecimal.ONE};
	 }
	 List<BigDecimal> weights = new ArrayList<>(count);
	 ThreadLocalRandom random = ThreadLocalRandom.current();
	 BigDecimal restOfTotal = BigDecimal.ONE;
	 for (int i = 0; i < count; i++) {
		 if (restOfTotal.doubleValue() == 0) {
			 weights.add(BigDecimal.ZERO);
			 continue;
		 }
		 if (i == count - 1) {
			 weights.add(restOfTotal);
			 break;
		 }
		 double rawWeight = random.nextDouble(1 + MathUtils.EPS);
		 BigDecimal precise = new BigDecimal(rawWeight).setScale(MathUtils.DEFAULT_PRECISION, RoundingMode.HALF_UP);
		 if (restOfTotal.compareTo(precise) > 0) {
			 restOfTotal = restOfTotal.subtract(precise);
			 weights.add(precise);
		 } else {
			 weights.add(restOfTotal);
			 restOfTotal = BigDecimal.ZERO;
		 }
	 }
	 Collections.shuffle(weights);
	 return weights.toArray(new BigDecimal[count]);
 }

}
