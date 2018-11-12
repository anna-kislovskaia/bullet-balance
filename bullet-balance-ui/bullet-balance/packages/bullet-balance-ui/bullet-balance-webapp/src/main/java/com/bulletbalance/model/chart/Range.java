package com.bulletbalance.model.chart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Range {
    @JsonProperty
    private double min;
    @JsonProperty
    private double max;
}
