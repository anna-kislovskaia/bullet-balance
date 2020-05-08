package com.bulletbalance.controller;

import com.bulletbalance.MoexDemo;
import com.bulletbalance.model.InstrumentProfile;
import com.bulletbalance.model.TangentPortfolioAnalytics;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.service.MoexInstrumentService;
import com.bulletbalance.service.MoexPortfolioService;
import com.bulletbalance.service.PortfolioAllocationService;
import com.bulletbalance.utils.PortfolioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/demo/moex")
public class MoexDemoController {
    private static final double RISK_FREE_RATE = 0.0025;
    private static final int DEFAULT_SAMPLE_COUNT = 10_000;
    @Autowired
    private PortfolioAllocationService portfolioAllocationService;
    @Autowired
    private MoexPortfolioService portfolioService;
    @Autowired
    private MoexInstrumentService instrumentService;

    @GetMapping(value = "/sample", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TangentPortfolioAnalytics getSampleAllocations(@RequestParam Optional<Integer> sampleCount,
                                                          @RequestParam Optional<Double> riskFreeRate) {
        final double baseRate = riskFreeRate.orElse(RISK_FREE_RATE);
        final int count = sampleCount.orElse(DEFAULT_SAMPLE_COUNT);
        final Portfolio<String> portfolio = MoexDemo.createPortfolio();
        return portfolioAllocationService.getTangentPortfolio(portfolio, baseRate, count);
    }

    @GetMapping(value = "/portfolio", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TangentPortfolioAnalytics getPortfolioAllocations(@RequestParam List<String> tickers,
                                                             @RequestParam int fromDate,
                                                             @RequestParam int toDate,
                                                             @RequestParam Optional<Integer> sampleCount,
                                                             @RequestParam Optional<Double> riskFreeRate) {
        final double baseRate = riskFreeRate.orElse(RISK_FREE_RATE);
        final int count = sampleCount.orElse(DEFAULT_SAMPLE_COUNT);
        final Portfolio<String> portfolio = portfolioService.createPortfolio(
                tickers, PortfolioUtils.intToLocalDate(fromDate), PortfolioUtils.intToLocalDate(toDate));
        return portfolioAllocationService.getTangentPortfolio(portfolio, baseRate, count);
    }

    @GetMapping(value = "/instruments", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<InstrumentProfile> getInstruments() {
        return instrumentService.getInstruments();
    }
}
