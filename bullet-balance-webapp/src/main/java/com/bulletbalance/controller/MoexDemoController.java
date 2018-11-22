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
    private static final LeastRiskyAllocationSelector LOWEST_RISK_SELECTOR = new LeastRiskyAllocationSelector();
    private static final TangentPortfolioSelector TANGENT_PORTFOLIO_SELECTOR = new TangentPortfolioSelector(PortfolioUtils.convertAnnualRateToDaily(RISK_FREE_RATE));

    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String test() {
        return "test";
    }

    @GetMapping(value = "/sample", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TangentPortfolioAnalytics getSampleAllocations(@RequestParam Optional<Integer> sampleCount, @RequestParam Optional<Integer> pointsCount) {
        AllocationSamplesGenerator analyzer = new AllocationSamplesGenerator();
        analyzer.setSamplesCount(sampleCount.orElseGet(() -> 10_000));
        analyzer.setWeightsGenerator(new NoShortSellWeightsGenerator());
        final Portfolio<String> portfolio = MoexDemo.createPortfolio();
        analyzer.setPortfolio(portfolio);
        List<AllocationResult> samples = analyzer.generate();
        AllocationResult lowestRisk = LOWEST_RISK_SELECTOR.selectResult(samples);
        AllocationResult tangentPortfolio = TANGENT_PORTFOLIO_SELECTOR.selectResult(samples);
        int count = pointsCount.orElseGet(() -> 200);
        ChartPlot plot = ChartUtils.createPlot(samples, count);
        return new TangentPortfolioAnalytics(portfolio.getAssetKeys(), lowestRisk, tangentPortfolio, plot, RISK_FREE_RATE);
    }
}
