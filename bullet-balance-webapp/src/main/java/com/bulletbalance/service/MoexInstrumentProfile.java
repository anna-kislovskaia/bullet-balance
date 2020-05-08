package com.bulletbalance.service;

import com.bulletbalance.model.InstrumentProfile;
import lombok.Getter;

@Getter
public class MoexInstrumentProfile extends InstrumentProfile {
    private String id;
    private String market;

    public MoexInstrumentProfile(String ticker, String name, String id, String market) {
        super(ticker, name);
        this.id = id;
        this.market = market;
    }
}
