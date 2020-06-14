package com.bulletbalance.service;

import com.bulletbalance.AllocationSamplesGenerator;
import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.analytics.LeastRiskyAllocationSelector;
import com.bulletbalance.analytics.TangentPortfolioSelector;
import com.bulletbalance.controller.MoexDemoController;
import com.bulletbalance.model.InstrumentFundamentals;
import com.bulletbalance.model.InstrumentStatistics;
import com.bulletbalance.model.TangentPortfolioAnalytics;
import com.bulletbalance.model.chart.ChartPlot;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.random.NoShortSellWeightsGenerator;
import com.bulletbalance.utils.ChartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioAllocationService {
    private static final LeastRiskyAllocationSelector LOWEST_RISK_SELECTOR = new LeastRiskyAllocationSelector();
    private static final Logger log = LoggerFactory.getLogger(MoexDemoController.class);

    @Autowired
    private MoexPriceRepository priceRepository;
    @Autowired
    private MoexFundamentalsRepository fundamentalsRepository;

    public TangentPortfolioAnalytics getTangentPortfolio(Portfolio<String> portfolio, double riskFreeRate, int sampleCount) {
        log.info("Calculating tangent portfolio for {} base rate {} and sample count {}",
                portfolio.getAssetKeys(), riskFreeRate, sampleCount);
        AllocationSamplesGenerator analyzer = new AllocationSamplesGenerator();
        analyzer.setSamplesCount(sampleCount);
        analyzer.setWeightsGenerator(new NoShortSellWeightsGenerator());
        analyzer.setPortfolio(portfolio);
        List<AllocationResult> samples = analyzer.generate()
                .stream()
                .map(allocationResult -> allocationResult.annualize(portfolio.getSize()))
                .collect(Collectors.toList());
        AllocationResult lowestRisk = LOWEST_RISK_SELECTOR.selectResult(samples);
        log.info("Lowest risk porfolio {}", lowestRisk);
        TangentPortfolioSelector portfolioSelector = new TangentPortfolioSelector(riskFreeRate);
        AllocationResult tangentPortfolio = portfolioSelector.selectResult(samples);
        if (tangentPortfolio == null) {
            tangentPortfolio = new AllocationResult(new BigDecimal[0], riskFreeRate, lowestRisk.getWeighthedRisk());
        }
        ChartPlot plot = ChartUtils.createPlot(samples, lowestRisk);
        List<InstrumentStatistics> lastPrices = portfolio.getAssetKeys().stream()
                .map(ticker -> {
                    Double lastPrice = priceRepository.getLast(ticker);
                    InstrumentFundamentals fundamentals = fundamentalsRepository.getInstrumentProfile(ticker);
                    return new InstrumentStatistics(fundamentals, lastPrice);
                })
                .collect(Collectors.toList());
        return new TangentPortfolioAnalytics(portfolio.getAssetKeys(), lastPrices, lowestRisk, tangentPortfolio, plot, riskFreeRate);
    }
}

