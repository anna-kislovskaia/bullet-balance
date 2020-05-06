package com.bulletbalance.controller;

import com.bulletbalance.AllocationSamplesGenerator;
import com.bulletbalance.MoexDemo;
import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.analytics.LeastRiskyAllocationSelector;
import com.bulletbalance.analytics.TangentPortfolioSelector;
import com.bulletbalance.model.TangentPortfolioAnalytics;
import com.bulletbalance.model.chart.ChartPlot;
import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.random.NoShortSellWeightsGenerator;
import com.bulletbalance.utils.ChartUtils;
import com.bulletbalance.utils.PortfolioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/demo/moex")
public class MoexDemoController {
    private static final double RISK_FREE_RATE = 0.0025;
    private static final int DEFAULT_SAMPLE_COUNT = 10_000;
    private static final LeastRiskyAllocationSelector LOWEST_RISK_SELECTOR = new LeastRiskyAllocationSelector();
    private static final Logger log = LoggerFactory.getLogger(MoexDemoController.class);

    @GetMapping(value = "/sample", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TangentPortfolioAnalytics getSampleAllocations(@RequestParam Optional<Integer> sampleCount,
                                                          @RequestParam Optional<Double> riskFreeRate) {
        final double baseRate = riskFreeRate.orElse(RISK_FREE_RATE);
        final int count = sampleCount.orElse(DEFAULT_SAMPLE_COUNT);
        final Portfolio<String> portfolio = MoexDemo.createPortfolio();
        AllocationSamplesGenerator analyzer = new AllocationSamplesGenerator();
        analyzer.setSamplesCount(count);
        analyzer.setWeightsGenerator(new NoShortSellWeightsGenerator());
        analyzer.setPortfolio(portfolio);
        log.info("Calculating MOEX portfolio for base rate {} and sample count {}", baseRate, count);
        List<AllocationResult> samples = analyzer.generate();
        AllocationResult lowestRisk = LOWEST_RISK_SELECTOR.selectResult(samples);
        log.info("Lowerst risk {}", lowestRisk);
        TangentPortfolioSelector portfolioSelector = new TangentPortfolioSelector(PortfolioUtils.convertAnnualRateToDaily(baseRate));
        AllocationResult tangentPortfolio = portfolioSelector.selectResult(samples);
        ChartPlot plot = ChartUtils.createPlot(samples, lowestRisk);
        return new TangentPortfolioAnalytics(portfolio.getAssetKeys(), lowestRisk, tangentPortfolio, plot, baseRate);
    }
}
