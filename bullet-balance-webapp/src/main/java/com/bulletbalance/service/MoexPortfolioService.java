package com.bulletbalance.service;

import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.portfolio.PortfolioBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MoexPortfolioService {
    private static final Logger log = LogManager.getLogger(MoexPortfolioService.class);

    @Autowired
    private MoexPriceRepository priceRepository;

    public Portfolio<String> createPortfolio(List<String> tickers, LocalDate fromDate, LocalDate toDate) {
        log.info("Loading trading data {} - {} for {} ", fromDate, toDate, tickers);
        List<Map<LocalDate, Double>> prices = tickers.stream().map(ticker -> priceRepository.getPrices(ticker, fromDate, toDate)).collect(Collectors.toList());
        // extract common dates
        Set<LocalDate> commonDates = prices.stream().filter(map -> !map.isEmpty()).map(Map::keySet).reduce(new HashSet<>(), (accumulator, set) -> {
            if (accumulator.isEmpty()) {
                accumulator.addAll(set);
            } else {
                accumulator.retainAll(set);
            }
            return accumulator;
        });
        log.info("Common dates count={}", commonDates.size());
        if (commonDates.isEmpty()) {
            throw new IllegalArgumentException("Prices cannot be loaded");
        }

        PortfolioBuilder<String> builder = new PortfolioBuilder<>();
        for (int i = 0; i < tickers.size(); i++) {
            Map<LocalDate, Double> instrumentPrices = prices.get(i);
            if (instrumentPrices.isEmpty()) {
                continue;
            }
            double[] commonDatePrices = instrumentPrices.entrySet().stream()
                    .filter(entry -> commonDates.contains(entry.getKey()))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .mapToDouble(entry -> entry.getValue())
                    .toArray();
            builder.add(tickers.get(i), commonDatePrices);
        }
        return builder.build();
    }


}
