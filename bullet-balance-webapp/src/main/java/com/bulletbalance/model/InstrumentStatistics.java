package com.bulletbalance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentStatistics {
    @JsonProperty
    private InstrumentFundamentals fundamentals;
    @JsonProperty
    private Double lastPrice;

}
