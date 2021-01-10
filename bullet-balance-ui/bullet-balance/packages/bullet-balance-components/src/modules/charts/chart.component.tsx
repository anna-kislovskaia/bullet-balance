import * as React from 'react';
import { Component } from 'react';
import { TChartData, TChartSeries, ChartSeriesType } from "./chart.model";
import { ComposedChart, XAxis, YAxis, Line, Legend, CartesianGrid, Tooltip, Area } from "recharts";
import './chart.component.scss';

export interface XYChartProps {
    chartData: TChartData,
    width: number,
    height: number
}

const X_AXIS_KEY = 'x'; 
const convertChartData = (chartData: TChartData) => {
    return chartData.series.reduce((accum: any[], series: TChartSeries) => {
        series.points.forEach(point => {
            const index = accum.findIndex((value: any) => point.x === value.x);
            if (index >= 0) {
                const item = accum[index];
                accum[index] = {...item, [series.key]: point.y};
            } else {
                const item = {[X_AXIS_KEY]: point.x, [series.key]: point.y };
                accum.push(item);
            }
        });
        return accum;
    }, [])
}

const getSeriesNameByKey = (seriesKey: string, chartData: TChartData) => {
    const series = chartData.series.find(s => s.key === seriesKey);
    return series ? series.name : seriesKey;
}

export class XYChartComponent extends Component<XYChartProps, {}> {
    render() {
        const { chartData, children } = this.props;
        const data = convertChartData(chartData);

        return (
            <div className="chart">
                <ComposedChart  width={600} height={300} data={data}>
                    <XAxis type="number" dataKey={X_AXIS_KEY}/>
                    <YAxis type="number" />
                    <Legend formatter={value => getSeriesNameByKey(value, chartData)}/>
                    <CartesianGrid strokeDasharray="3 3" />
                    <Tooltip />
                    {chartData.series.map(entity => this.renderPlot(entity))}
                    {children}
                </ComposedChart >
            </div>
        );
    }

    renderPlot = (series: TChartSeries) => {
        switch(series.type) {
            case ChartSeriesType.LINEAR:
                return (
                    <Line key={series.key} dataKey={series.key} stroke={series.color}/>
                );
            case ChartSeriesType.RANGE:
                return (
                    <Area key={series.key} dataKey={series.key} stroke={series.color}/>
                );
               }
    }
}