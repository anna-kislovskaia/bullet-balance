import { ComponentClass, Component } from 'react';
import { RxProperties, rxComponentFactory} from "../../../utils/rx.utils";
import {TangentPortfolioComponent, TangentPortfolioComponentProps} from "../tangent-portoflio.component";
import { Shape, TChartData} from "../../charts/chart.model";
import {MoexDemoService} from "../../../services/moex-demo.service";
import {map} from "rxjs/internal/operators";
import {TPoint, TTangentPortfolio} from "../../../model/data.model";
import {Task, TaskSuccess} from "../../../utils/task.model";
import {TPlotLegend} from "../../charts/legend/legend.model";

type DefaultProperties = 'width' | 'height';

const props$: RxProperties<DefaultProperties, TangentPortfolioComponentProps> = () => {
    const chartData$ = MoexDemoService.getMoexSampleCurve()
        .pipe(map((task: Task<TTangentPortfolio>) => {
            if (task.isSuccess()) {
                const data = (task as TaskSuccess<TTangentPortfolio>).value;
                const lowestRiskReturn = data.lowest.y;
                const upperPoints = data.points.filter(point => point.y >= lowestRiskReturn);
                const lowerPoints = data.points.filter(point => point.y <= lowestRiskReturn);
                const maxRisk = data.points[data.points.length - 1].x;
                const coef = (data.tangent.y - data.riskFree.y)/ data.tangent.x;
                const maxTangentY = (maxRisk - data.tangent.x) * coef + data.riskFree.y;
                const tangentPoints: TPoint[] = [
                    data.riskFree,
                    data.tangent,
                    {x: maxRisk, y: maxTangentY}
                ];
                const chartData: TChartData = {
                    series: [
                        {points: upperPoints, name: "test name", color: "#79C7E3", key: "upper-series"},
                        {points: lowerPoints, name: "test name", color: "#79C7E3", key: "lower-series"},
                        {points: tangentPoints, name: "Tangent", color: "#79C700", key: "tangent-series"},
                    ],
                    enableTime: false
                };
                const legendData: TPlotLegend = { name: "test name", color: "#79C7E3", shape: Shape.line};
                return { chartData, legend: [legendData] };
            } else {
                return {
                    chartData: {series: [], enableTime: false},
                    legend: undefined
                }
            }
        }));

    return chartData$;
};

export const TangentPortfolioContainer = rxComponentFactory<DefaultProperties, TangentPortfolioComponentProps>(props$)(TangentPortfolioComponent);
