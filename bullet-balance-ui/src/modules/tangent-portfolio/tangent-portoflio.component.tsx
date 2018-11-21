import * as React from 'react';
import { Component } from "react";
import { XYChartComponent } from "../charts/chart.component";
import { TChartData } from "../charts/chart.model";
import { ChartLegend } from "../charts/legend/legend.component";
import { TPlotLegend } from "../charts/legend/legend.model";

export interface TangentPortfolioComponentProps {
    chartData: TChartData,
    width: number,
    height: number
    legend?: TPlotLegend[];
}

export class TangentPortfolioComponent extends Component<TangentPortfolioComponentProps, {}> {
    render() {
        const { legend, chartData } = this.props;
        return (
            <div>
                {chartData && <XYChartComponent chartData={chartData} {...this.props} />}
                {legend && <ChartLegend items={legend}/>}
            </div>
        );
    }
}