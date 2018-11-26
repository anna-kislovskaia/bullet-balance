import { ComponentClass, Component } from 'react';
import { RxProperties, rxComponentFactory} from "../../../utils/rx.utils";
import {TangentPortfolioComponent, TangentPortfolioComponentProps} from "../tangent-portoflio.component";
import { Shape, TChartData} from "../../charts/chart.model";
import {MoexDemoService} from "../../../services/moex-demo.service";
import {map, shareReplay} from "rxjs/internal/operators";
import {TPoint} from "../../../model/data.model";
import {Task, TaskUtils} from "../../../utils/task.model";
import {TPlotLegend} from "../../charts/legend/legend.model";
import {Observable, combineLatest} from "rxjs/index";

type ExternalProperties = 'width' | 'height';

const props$: RxProperties<ExternalProperties, TangentPortfolioComponentProps> = () => {
    const data$ = MoexDemoService.getMoexSampleCurve().pipe(shareReplay(1));

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

    return combineLatest(chartData$, legendData$).pipe(map(([chartData, legend]) => {
        return {chartData, legend}
    }));
};

const defaults: Partial<TangentPortfolioComponentProps> = {
    chartData: TaskUtils.pending,
    legend: TaskUtils.pending
};

export const TangentPortfolioContainer = rxComponentFactory<ExternalProperties, TangentPortfolioComponentProps>(props$, defaults)(TangentPortfolioComponent);
