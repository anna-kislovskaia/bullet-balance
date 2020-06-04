package com.bulletbalance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class InstrumentFundamentals {
    @JsonProperty
    int lotSize;

    @JsonProperty
    BigDecimal priceToEarnings;

    @JsonProperty
    BigDecimal priceToBookValue;
}
