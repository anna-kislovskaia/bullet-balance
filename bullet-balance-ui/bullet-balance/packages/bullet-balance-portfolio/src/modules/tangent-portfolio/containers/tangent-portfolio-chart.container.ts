import { RxProperties, rxComponentFactory} from "../../../utils/rx.utils";
import {
    AllocationItem, PortfolioAllocation, TangentPortfolioChartComponent,
    TangentPortfolioChartProps
} from "../components/tangent-portoflio-chart.component";
import { Shape, TChartData, TPlotLegend, ChartSeriesType, TRangeChartPoint, TNumberChartPoint} from "@bullet-balance/components";
import {MoexDemoService} from "../../../services/moex-demo.service";
import {distinctUntilChanged, map, shareReplay, switchMap} from "rxjs/internal/operators";
import {TPoint} from "../../../model/data.model";
import {Task, TaskUtils} from "../../../utils/task.model";
import {Observable, combineLatest} from "rxjs/index";

type ExternalProperties = 'width' | 'height' | 'samplesCount' | 'baseRate';

const defaults: Partial<TangentPortfolioChartProps> = {
    chartData: TaskUtils.pending,
    legend: TaskUtils.pending,
    allocation: TaskUtils.pending
};

const pointRiskComparator = (point1: TPoint, point2: TPoint): number => {
    return point1.x - point2.x;
}

const  convertValue = (value: number): number => {
    return Math.round(value * 10000) / 100;
}

const convertPoint = (point: TPoint): TNumberChartPoint => {
    return {x: convertValue(point.x), y: convertValue(point.y)}
}

const props$: RxProperties<ExternalProperties, TangentPortfolioChartProps> = (props$) => {
    const samplesCount$ = props$
        .pipe(map(value => value.samplesCount))
        .pipe(distinctUntilChanged())
        .pipe(shareReplay(1));
    const baseRate$ = props$
        .pipe(map(value => value.baseRate / 100))
        .pipe(distinctUntilChanged())
        .pipe(shareReplay(1));
    const data$ = combineLatest(samplesCount$, baseRate$)
        .pipe(switchMap(([count, baseRate]) => MoexDemoService.getMoexSampleCurve(count, baseRate)))
        .pipe(shareReplay(1));

    const chartData$: Observable<Task<TChartData>> = data$
        .pipe(map(task => task.map( data => {
            const lowestRiskReturn = data.lowest.y;
            const upperPoints = data.points.filter(point => point.y >= lowestRiskReturn).sort(pointRiskComparator);
            const lowerPoints = data.points.filter(point => point.y <= lowestRiskReturn).sort(pointRiskComparator);
            const maxRisk = data.points[data.points.length - 1].x;
            const coef = (data.tangent.y - data.riskFree.y) / data.tangent.x;
            const maxTangentY = (maxRisk - data.tangent.x) * coef + data.tangent.y;
            const tangentPoints: TNumberChartPoint[] = [
                convertPoint(data.riskFree),
                convertPoint(data.tangent),
                convertPoint({x: maxRisk, y: maxTangentY})
            ];
            const distribution: TRangeChartPoint[] = data.points.map(point => {
                let upper: number = upperPoints.findIndex(u => u.x >= point.x);
                upper = upper >= 0 ? upper : upperPoints.length - 1;
                let low: number = lowerPoints.findIndex(l => l.x >= point.x);
                low = low >= 0 ? low : lowerPoints.length - 1;
                return {x: convertValue(point.x), y: [convertValue(lowerPoints[low].y), convertValue(upperPoints[upper].y)]}
            }); 
            distribution.push({x: 0, y: []}) 
            return {
                series: [
                    {points: distribution, name: "Risk/Return distribution", color: "#79C7E3", key: "distribution-series", type: ChartSeriesType.RANGE},
                    {points: tangentPoints, name: "Tangent Portfolio", color: "#79C700", key: "tangent-series", type: ChartSeriesType.LINEAR},
                ],
                enableTime: false
            };
        })));

    const legendData$: Observable<Task<TPlotLegend[]>> = data$.pipe(map(task => task.map(() => {
        return [
            { name: "Risk/Return distribution", color: "#79C7E3", shape: Shape.line},
            { name: "Tangent Portfolio", color: "#79C700", shape: Shape.line},
        ];
    })));

    const allocation$: Observable<Task<PortfolioAllocation>> = data$.pipe(map(task => task.map(data => {
        const weights = data.portfolio.weights;
        const allocations: AllocationItem[] = data.portfolio.instruments.map((ticker, index) => {
            return {ticker, weight: weights[index]};
        });
        allocations.sort((a1, a2) => a1.weight - a2.weight);
        return {risk: data.tangent.x, performance: data.tangent.y, allocations};
    })));

    return combineLatest(chartData$, legendData$, allocation$).pipe(map(([chartData, legend, allocation]) => {
        return {chartData, legend, allocation}
    }));
};

export const TangentPortfolioChartContainer = rxComponentFactory<ExternalProperties, TangentPortfolioChartProps>(props$, defaults)(TangentPortfolioChartComponent);
