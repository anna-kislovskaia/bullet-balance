package com.bulletbalance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentProfile {
    @JsonProperty
    private String ticker;
    @JsonProperty
    private String name;

    public static Comparator<InstrumentProfile> TICKER_COMPARATOR = new Comparator<InstrumentProfile>() {
        @Override
        public int compare(InstrumentProfile o1, InstrumentProfile o2) {
            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return o1.ticker.compareTo(o2.ticker);
        }
    };
}
