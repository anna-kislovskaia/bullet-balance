package com.bulletbalance.utils;

import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.model.chart.ChartPlot;
import com.bulletbalance.model.chart.Point;
import com.bulletbalance.model.chart.Range;

import java.util.ArrayList;
import java.util.List;

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

        List<Point> points = new ArrayList<>(allocations.size());
        Range xRange = new Range().setMax(Double.MIN_VALUE).setMin(Double.MAX_VALUE);
        Range yRange = new Range().setMax(Double.MIN_VALUE).setMin(Double.MAX_VALUE);

        for (AllocationResult allocation : allocations) {
            points.add(new Point().setX(allocation.getWeighthedRisk()).setY(allocation.getWeightedReturn()));
            xRange.setMax(Math.max(xRange.getMax(), allocation.getWeighthedRisk()));
            xRange.setMin(Math.min(xRange.getMin(), allocation.getWeighthedRisk()));
            yRange.setMax(Math.max(yRange.getMax(), allocation.getWeightedReturn()));
            yRange.setMin(Math.min(yRange.getMin(), allocation.getWeightedReturn()));
        }

        return new ChartPlot(xRange, yRange, points);
    }
}
