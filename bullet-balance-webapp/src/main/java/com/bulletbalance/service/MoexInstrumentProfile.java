package com.bulletbalance.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class MoexInstrumentProfile {
    private String ticker;
    private String name;
    private String id;
    private String market;
}
