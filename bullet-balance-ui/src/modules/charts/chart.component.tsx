import * as React from 'react';
import { Component } from 'react';
import {TChartData, TChartSeries} from "./chart.model";
import {XYPlot, LineSeries, HorizontalGridLines, DiscreteColorLegend, XAxis, YAxis} from "react-vis";

export interface XYChartProps { chartData: TChartData }

export class XYChartComponent extends Component<XYChartProps, {}> {
    render() {
        const { chartData } = this.props;
        const legend = chartData.series.map(serie => {
           return {title: serie.name, color: serie.color};
        });

        return (
            <XYPlot width={300} height={300} >
                <HorizontalGridLines/>
                {chartData.series.map(entity => this.renderRow(entity))}
                <XAxis/>
                <YAxis/>
                <DiscreteColorLegend orientation="horizontal" items={legend}/>
            </XYPlot>
        );
    }

    renderRow = (series: TChartSeries) => {
        return (
            <LineSeries key={series.name} data={series.points} color={series.color}/>
        );
    }
}