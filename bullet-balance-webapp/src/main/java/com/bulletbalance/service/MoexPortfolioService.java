package com.bulletbalance.service;

import com.bulletbalance.portfolio.AggregationPeriod;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.portfolio.PortfolioBuilder;
import net.bytebuddy.asm.Advice;
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
        TreeSet<LocalDate> commonDates = (TreeSet<LocalDate>)prices.stream()
                .filter(map -> !map.isEmpty()).map(Map::keySet)
                .reduce(new TreeSet<>(), (accumulator, set) -> {
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

        AggregationPeriod period = AggregationPeriod.getPeriod(fromDate, toDate);
        LocalDate lastDate = period.getNextDate(commonDates.last());
        Set<LocalDate> selected = new TreeSet<>();
        for (LocalDate date = commonDates.first(); date.isBefore(lastDate); date = period.getNextDate(date)) {
            selected.add(commonDates.floor(date));
        }
        selected.add(commonDates.last());
        log.info("Filtered common dates count={}", selected.size());

        PortfolioBuilder<String> builder = new PortfolioBuilder<>();
        builder.setAggregationPeriod(period);
        for (int i = 0; i < tickers.size(); i++) {
            Map<LocalDate, Double> instrumentPrices = prices.get(i);
            if (instrumentPrices.isEmpty()) {
                continue;
            }
            double[] commonDatePrices = instrumentPrices.entrySet().stream()
                    .filter(entry -> selected.contains(entry.getKey()))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .mapToDouble(Map.Entry::getValue)
                    .toArray();
            builder.add(tickers.get(i), commonDatePrices);
        }
        return builder.build();
    }


}
