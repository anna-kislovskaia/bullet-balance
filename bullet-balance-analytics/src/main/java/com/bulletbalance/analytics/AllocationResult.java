package com.bulletbalance.analytics;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Sample of asset allocation weights
 */
public class AllocationResult {
	public static Comparator<AllocationResult> RISK_COMPARATOR = (r1, r2) -> {
		int result = Double.compare(r1.allocationStd, r2.allocationStd);
		if (result == 0) {
			return Double.compare(r1.allocationReturn, r2.allocationReturn);
		}
		return result;
	};

	private final BigDecimal[] allocation;
	private final double allocationReturn;
	private final double allocationStd;

	public AllocationResult(BigDecimal[] allocation, double allocationReturn, double allocationStd) {
		this.allocation = allocation;
		this.allocationReturn = allocationReturn;
		this.allocationStd = allocationStd;
	}

	public BigDecimal[] getAllocation() {
		return allocation;
	}

	public double getAllocationReturn() {
		return allocationReturn;
	}

	public double getAllocationStd() {
		return allocationStd;
	}

	@Override
	public String toString() {
		return "AllocationResult{" +
				"return=" + allocationReturn +
				", risk=" + allocationStd +
				'}';
	}
}
