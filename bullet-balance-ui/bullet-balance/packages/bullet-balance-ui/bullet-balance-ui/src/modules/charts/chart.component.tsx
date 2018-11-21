import * as React from 'react';
import { Component, Fragment } from 'react';
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

        const xScaleType = chartData.enableTime ? 'time' : 'linear';
        return (
            <Fragment>
                <XYChart {...this.props}
                         xScale={{ type: xScaleType }}
                         yScale={{ type: 'linear' }}
                         ariaLabel={'test label'} >
                    <XAxis />
                    <YAxis orientation={'left'}  />
                    {chartData.series.map(entity => this.renderPlot(entity))}
                    {children}
                </XYChart>
                {this.renderLegend(chartData)}
            </Fragment>
        );
    }

    renderPlot = (series: TChartSeries) => {
        return (
            <LineSeries key={series.name} seriesKey={series.name} data={series.points} stroke={series.color}/>
        );
    }

    renderLegend = (chartData: TChartData) => {
        return (
            <div>
                {chartData.series.map(serie => {
                    const style = {stroke: serie.color, strokeWidth : 2};
                    return (
                        <div key={`legend-${serie.name}`}>
                            <svg height="10" width="20">
                                <line x1="0" y1="5" x2="15" y2="5" style={style} />
                            </svg>
                            <span>{serie.name}</span>
                        </div>
                    );
                })}
            </div>
        );
    }
}