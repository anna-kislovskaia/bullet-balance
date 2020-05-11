package com.bulletbalance.analytics;

import com.bulletbalance.utils.PortfolioUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Sample of asset weights weights
 */
@AllArgsConstructor
@Data
public class AllocationResult {
	public static Comparator<AllocationResult> RISK_COMPARATOR = (r1, r2) -> {
		int result = Double.compare(r1.weighthedRisk, r2.weighthedRisk);
		if (result == 0) {
			return Double.compare(r1.weightedReturn, r2.weightedReturn);
		}
		return result;
	};

	@NotNull
	@NonNull
	@JsonProperty
	private final BigDecimal[] weights;
	@NotNull
	@NonNull
	@JsonProperty
	private final double weightedReturn;

	@NotNull
	@NonNull
	@JsonProperty
	private final double weighthedRisk;

	@Override
	public String toString() {
		return "AllocationResult{" +
				"return=" + weightedReturn +
				", risk=" + weighthedRisk +
				'}';
	}

	public AllocationResult annualize(double size) {
		return new AllocationResult(
				weights,
				PortfolioUtils.convertDailyRateToAnnual(weightedReturn),
				PortfolioUtils.convertDailyRiskToAnnual(weighthedRisk, size));
	}
}
