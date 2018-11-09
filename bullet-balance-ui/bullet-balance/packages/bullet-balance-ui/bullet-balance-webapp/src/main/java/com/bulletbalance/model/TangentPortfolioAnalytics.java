package com.bulletbalance.model;

import com.bulletbalance.analytics.AllocationResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class TangentPortfolioAnalytics {
    @NotNull @NonNull
    @JsonProperty
    List<String> instruments;

    @NotNull @NonNull
    @JsonProperty
    AllocationResult lowestRiskAllocation;

    @NotNull @NonNull
    @JsonProperty
    AllocationResult tangentPortfolioAllocation;

    @NotNull @NonNull
    @JsonProperty
    List<AllocationResult> samples;

}