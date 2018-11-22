import { ComponentClass, Component } from 'react';
import { RxProperties, rxComponentFactory} from "../../../utils/rx.utils";
import {TangentPortfolioComponent, TangentPortfolioComponentProps} from "../tangent-portoflio.component";
import { Shape, TChartData} from "../../charts/chart.model";
import {MoexDemoService} from "../../../services/moex-demo.service";
import {map} from "rxjs/internal/operators";
import {TTangentPortfolio} from "../../../model/data.model";
import {Task, TaskSuccess} from "../../../utils/task.model";
import {TPlotLegend} from "../../charts/legend/legend.model";

type DefaultProperties = 'width' | 'height';

const props$: RxProperties<DefaultProperties, TangentPortfolioComponentProps> = () => {
    const chartData$ = MoexDemoService.getMoexSampleCurve()
        .pipe(map((task: Task<TTangentPortfolio>) => {
            if (task.isSuccess()) {
                const portfolioData = (task as TaskSuccess<TTangentPortfolio>).value;
                const lowestRiskReturn = portfolioData.lowest.y;
                const upperPoints = portfolioData.points.filter(point => point.y >= lowestRiskReturn);
                const lowerPoints = portfolioData.points.filter(point => point.y <= lowestRiskReturn);
                const chartData: TChartData = {
                    series: [
                        {points: upperPoints, name: "test name", color: "#79C7E3"},
                        {points: lowerPoints, name: "test name", color: "#79C7E3"},
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
