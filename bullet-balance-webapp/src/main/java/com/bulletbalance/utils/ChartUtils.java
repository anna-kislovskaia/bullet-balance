package com.bulletbalance.utils;

import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.analytics.TangentPortfolioSelector;
import com.bulletbalance.model.chart.ChartPlot;
import com.bulletbalance.model.chart.Point;
import com.bulletbalance.model.chart.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChartUtils {

    /**
     * picks min and max for each point within epsilon {@link MathUtils#EPS}
     * @param allocations must be sorted with {@link AllocationResult#RISK_COMPARATOR}
     * @return plot data
     */
    public static ChartPlot createPlot(List<AllocationResult> allocations, AllocationResult bendPoint) {
        if (allocations.size() < 2) {
            throw new IllegalArgumentException("Samples count is insufficient");
        }

        List<AllocationResult> upperArc = TangentPortfolioSelector.filterSamples(
                allocations.stream().filter(sample -> sample.getWeightedReturn() > bendPoint.getWeightedReturn()).collect(Collectors.toList()),
                TangentPortfolioSelector.RETURN_COMPARATOR);
        List<AllocationResult> lowerArc = TangentPortfolioSelector.filterSamples(
                allocations.stream().filter(sample -> sample.getWeightedReturn() < bendPoint.getWeightedReturn()).collect(Collectors.toList()),
                TangentPortfolioSelector.RETURN_COMPARATOR.reversed());

        List<AllocationResult> curve = new ArrayList<>(allocations.size());
        curve.add(bendPoint);
        curve.addAll(upperArc);
        curve.addAll(lowerArc);
        curve.sort(AllocationResult.RISK_COMPARATOR);

        List<Point> points = curve.stream()
                .map(sample -> new Point().setX(sample.getWeighthedRisk()).setY(sample.getWeightedReturn()))
                .collect(Collectors.toList());
        Range xRange = new Range().setMin(points.get(0).getX()).setMax(points.get(points.size() - 1).getX());
        Range yRange = new Range()
                .setMin(lowerArc.get(lowerArc.size() - 1).getWeightedReturn())
                .setMax(upperArc.get(upperArc.size() - 1).getWeightedReturn());
        return new ChartPlot(xRange, yRange, points);
    }
}
