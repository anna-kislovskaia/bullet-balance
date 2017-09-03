package com.bulletbalance.analytics;

import java.util.List;

/**
 * Selects allocation with the lowest risk level
 */
public class LeastRiskyAllocationSelector implements AllocationResultSelector {
	@Override
	public AllocationResult selectResult(List<AllocationResult> results) {
		return results.isEmpty() ? null : results.get(0);
	}
}
