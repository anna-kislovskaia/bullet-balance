import * as React from 'react';
import { Component } from 'react';
import { TChartData, TChartSeries } from "./chart.model";
import { XYChart, XAxis, YAxis, LineSeries } from "@data-ui/xy-chart";

export interface XYChartProps {
    chartData: TChartData,
    width: number,
    height: number
}

export class XYChartComponent extends Component<XYChartProps, {}> {
    render() {
        const { chartData, children } = this.props;
        const legend = chartData.series.map(serie => {
           return {title: serie.name, color: serie.color};
        });

        const xScaleType = chartData.enableTime ? 'time' : 'linear';
        return (
            <XYChart {...this.props}
                     xScale={{ type: xScaleType }}
                     yScale={{ type: 'linear' }}
                     ariaLabel={'test label'} >
                <XAxis />
                <YAxis orientation={'left'}  />
                {chartData.series.map(entity => this.renderPlot(entity))}
                {children}
            </XYChart>
        );
    }

    renderPlot = (series: TChartSeries) => {
        return (
            <LineSeries key={series.name} seriesKey={series.name} data={series.points} stroke={series.color}/>
        );
    }
}