package com.bulletbalance.utils;

import com.bulletbalance.analytics.AllocationResult;
import com.bulletbalance.model.chart.ChartPlot;
import com.bulletbalance.model.chart.Point;
import com.bulletbalance.model.chart.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChartUtils {
    private static final double EPS = 0.000_005;

    /**
     * picks min and max for each point within epsilon {@link #EPS}
     * @param allocations must be sorted with {@link AllocationResult#RISK_COMPARATOR}
     * @return plot data
     */
    public static ChartPlot createPlot(List<AllocationResult> allocations, int maxPointCount) {
        if (allocations.size() < 2) {
            throw new IllegalArgumentException("Samples count is insufficient");
        }
        Range xRange = new Range()
                .setMin(allocations.get(0).getWeigthedRisk())
                .setMax(allocations.get(allocations.size() - 1).getWeigthedRisk());
        double xdistance = xRange.getMax() - xRange.getMin();
        double invervalsCount = maxPointCount / 2;
        double eps = xdistance / invervalsCount;
        Point maxPoint = null, minPoint = null;
        List<Point> points = new ArrayList<>();
        Range yRange = null;
        double start = Double.MIN_VALUE;
        for(AllocationResult sample : allocations) {
            double x = sample.getWeigthedRisk();
            double y = sample.getWeightedReturn();
            if (start + eps < x) {
                // register point
                registerPointsOnInterval(minPoint, maxPoint, points);
                // reset point
                maxPoint = updatePoint(sample, null, Math::max);
                minPoint = updatePoint(sample, null, Math::min);
                start = x;
            } else {
                maxPoint = updatePoint(sample, maxPoint, Math::max);
                minPoint = updatePoint(sample, minPoint, Math::min);
            }
            if (yRange == null) {
                yRange = new Range().setMin(y).setMax(y);
            } else {
                yRange.setMin(Math.min(yRange.getMin(), y)).setMax(Math.max(yRange.getMax(), y));
            }
        }
        // register point
        registerPointsOnInterval(minPoint, maxPoint, points);
        return new ChartPlot(xRange, yRange, points);
    }

    private static void registerPointsOnInterval(Point maxPoint, Point minPoint, List<Point> points) {
        if (maxPoint != null) {
            points.add(maxPoint);
        }
        if (minPoint != null && !minPoint.equals(maxPoint)) {
            points.add(minPoint);
        }
    }

    private static Point updatePoint(AllocationResult sample, Point point, BiFunction<Double, Double, Double> selector) {
        double x = sample.getWeigthedRisk();
        double y = sample.getWeightedReturn();
        if (point == null || selector.apply(y, point.getY()) == y) {
            return new Point().setX(x).setY(y);
        }
        return point;
    }
}
