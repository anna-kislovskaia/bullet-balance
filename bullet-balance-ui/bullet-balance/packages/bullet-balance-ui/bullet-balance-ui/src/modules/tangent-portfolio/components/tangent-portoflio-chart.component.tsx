import * as React from 'react';
import { Component } from "react";
import { XYChartComponent } from "../../charts/chart.component";
import { TChartData } from "../../charts/chart.model";
import { ChartLegend } from "../../charts/legend/legend.component";
import { TPlotLegend } from "../../charts/legend/legend.model";
import {Task} from "../../../utils/task.model";

export interface TangentPortfolioChartProps {
    chartData: Task<TChartData>;
    width: number;
    height: number;
    samplesCount: number;
    legend: Task<TPlotLegend[]>;
}

export class TangentPortfolioChartComponent extends Component<TangentPortfolioChartProps, {}> {
    render() {
        const { legend, chartData, width, height } = this.props;
        return (
            <div>
                {chartData.getNullable() && <XYChartComponent chartData={chartData.getNullable()} width={width} height={height}/>}
                {legend.getNullable() && <ChartLegend items={legend.getNullable()}/>}
            </div>
        );
    }
}