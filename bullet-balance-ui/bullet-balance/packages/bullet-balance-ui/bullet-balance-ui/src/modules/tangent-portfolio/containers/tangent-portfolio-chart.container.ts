import { ComponentClass, Component } from 'react';
import { RxProperties, rxComponentFactory} from "../../../utils/rx.utils";
import {
    AllocationItem, PortfolioAllocation, TangentPortfolioChartComponent,
    TangentPortfolioChartProps
} from "../components/tangent-portoflio-chart.component";
import { Shape, TChartData} from "../../charts/chart.model";
import {MoexDemoService} from "../../../services/moex-demo.service";
import {distinctUntilChanged, map, shareReplay, switchMap} from "rxjs/internal/operators";
import {TPoint} from "../../../model/data.model";
import {Task, TaskUtils} from "../../../utils/task.model";
import {TPlotLegend} from "../../charts/legend/legend.model";
import {Observable, combineLatest} from "rxjs/index";

type ExternalProperties = 'width' | 'height' | 'samplesCount';

const defaults: Partial<TangentPortfolioChartProps> = {
    chartData: TaskUtils.pending,
    legend: TaskUtils.pending,
    allocation: TaskUtils.pending
};

const props$: RxProperties<ExternalProperties, TangentPortfolioChartProps> = (props$) => {
    const samplesCount$ = props$
        .pipe(map(value => value.samplesCount))
        .pipe(distinctUntilChanged())
        .pipe(shareReplay(1));
    const data$ = samplesCount$.pipe(
        switchMap(count => MoexDemoService.getMoexSampleCurve(count).pipe(shareReplay(1)))
    );

    const chartData$: Observable<Task<TChartData>> = data$
        .pipe(map(task => task.map( data => {
            const lowestRiskReturn = data.lowest.y;
            const upperPoints = data.points.filter(point => point.y >= lowestRiskReturn);
            const lowerPoints = data.points.filter(point => point.y <= lowestRiskReturn);
            const maxRisk = data.points[data.points.length - 1].x;
            const coef = (data.tangent.y - data.riskFree.y) / data.tangent.x;
            const maxTangentY = (maxRisk - data.tangent.x) * coef + data.tangent.y;
            const tangentPoints: TPoint[] = [
                data.riskFree,
                data.tangent,
                {x: maxRisk, y: maxTangentY}
            ];
            return {
                series: [
                    {points: upperPoints, name: "test name", color: "#79C7E3", key: "upper-series"},
                    {points: lowerPoints, name: "test name", color: "#79C7E3", key: "lower-series"},
                    {points: tangentPoints, name: "Tangent", color: "#79C700", key: "tangent-series"},
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
