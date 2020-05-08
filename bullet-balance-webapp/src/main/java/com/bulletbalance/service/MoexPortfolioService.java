package com.bulletbalance.service;

import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.portfolio.PortfolioBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MoexPortfolioService {
    private static final Logger log = LogManager.getLogger(MoexPortfolioService.class);

    @Autowired
    private MoexPriceRepository priceRepository;

    public Portfolio<String> createPortfolio(List<String> tickers, LocalDate fromDate, LocalDate toDate) {
        log.info("Loading trading data {} - {} for {} ", fromDate, toDate, tickers);
        PortfolioBuilder<String> builder = new PortfolioBuilder<>();
        tickers.forEach(ticker -> {
            List<Double> prices = priceRepository.getPrices(ticker, fromDate, toDate);
            if (!prices.isEmpty()) {
                builder.add(ticker, prices.stream().mapToDouble(value -> value).toArray());
            }
        });
        return builder.build();
    }


}
