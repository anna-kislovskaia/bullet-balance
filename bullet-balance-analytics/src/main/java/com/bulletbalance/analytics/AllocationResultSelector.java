package com.bulletbalance.analytics;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Selects {@linkplain AllocationResult} which matches particular criteria
 */
public interface AllocationResultSelector {

	/**
	 * Selects best match for criteria
	 * @param results sample allocations
	 * @return matching allocation or {@code null}
	 */
	@Nullable
	AllocationResult selectResult(List<AllocationResult> results);
}
