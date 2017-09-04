package com.bulletbalance.analytics;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Selects allocation with the lowest risk level
 */
public class LeastRiskyAllocationSelector implements AllocationResultSelector {
	@Override
	@Nullable
	public AllocationResult selectResult(List<AllocationResult> results) {
		return results.isEmpty() ? null : results.get(0);
	}
}
