package com.bulletbalance.service;

import com.bulletbalance.model.InstrumentProfile;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class MoexInstrumentService {
    private static final Logger log = LogManager.getLogger(MoexInstrumentService.class);
    private static final String MOEX_EQUITY_MARKET = "1";
    private Map<String, MoexInstrumentProfile> instruments = new HashMap<>();

    @Value("classpath:instruments.csv")
    Resource resourceFile;

    @PostConstruct
    private void init() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceFile.getInputStream()));
        bufferedReader.lines()
                .map(line -> {
                    String[] tokens = line.split(";");
                    return new MoexInstrumentProfile(tokens[2], tokens[1], tokens[0], tokens[3]);
                })
                .filter(profile -> MOEX_EQUITY_MARKET.equals(profile.getMarket()))
                .forEach(profile -> {
                    MoexInstrumentProfile registered = instruments.putIfAbsent(profile.getTicker(), profile);
                    if (registered != null && registered != profile) {
                        log.warn("Duplicate profile {}\n -> {}", profile, registered);
                    }
                });
    }

    public MoexInstrumentProfile getProfile(String ticker) {
        MoexInstrumentProfile profile = instruments.get(ticker);
        Preconditions.checkArgument(profile != null, "Unknown ticker " + ticker);
        return profile;
    }

    public List<InstrumentProfile> getInstruments() {
        ArrayList<InstrumentProfile> profiles = new ArrayList<>(instruments.values());
        profiles.sort(InstrumentProfile.TICKER_COMPARATOR);
        return profiles;
    }
}
