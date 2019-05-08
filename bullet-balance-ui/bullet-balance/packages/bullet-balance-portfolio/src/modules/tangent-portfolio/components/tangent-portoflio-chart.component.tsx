import * as React from 'react';
import { Component, Fragment} from "react";
import { XYChartComponent, TChartData, ChartLegend, TPlotLegend } from "bullet-balance-components";
//import { XYChartComponent } from "../../charts/chart.component";
//import { TChartData } from "../../charts/chart.model";
//import { ChartLegend } from "../../charts/legend/legend.component";
//import { TPlotLegend } from "../../charts/legend/legend.model";
import {Task} from "../../../utils/task.model";
import { LoadingIndicatorComponent } from '../../loading-indicator/loading-indicator.component';

export type AllocationItem = {
    ticker: string;
    weight: number;
}

export type PortfolioAllocation = {
    allocations: AllocationItem[];
    risk: number;
    performance: number;
}

export interface TangentPortfolioChartProps {
    chartData: Task<TChartData>;
    allocation: Task<PortfolioAllocation>;
    width: number;
    height: number;
    samplesCount: number;
    legend: Task<TPlotLegend[]>;
}

type ResultRenderer<T> = (result: T) => JSX.Element;
function taskRenderer<T>(task: Task<T>, resultRenderer: ResultRenderer<T>): JSX.Element {
    const result = task.getNullable();
    if(result) {
        return resultRenderer(result);
    } else {
        return (
            <div><LoadingIndicatorComponent/></div>
        );
    }
}

export class TangentPortfolioChartComponent extends Component<TangentPortfolioChartProps, {}> {
    render() {
        const { legend, chartData, allocation, width, height } = this.props;
        return (
            <div>
                {taskRenderer(chartData, (result) => <XYChartComponent chartData={result} width={width} height={height}/>)}
                {legend.getNullable() && <ChartLegend items={legend.getNullable()}/>}
                {allocation.getNullable() && this.renderAllocations(allocation.getNullable())}
            </div>
        );
    }

      renderAllocations = (portfolio: PortfolioAllocation) => {
        if (portfolio.allocations.length === 0) {
            return null;
        }
        const risk = (portfolio.risk * 100).toFixed(2);
        const performance = (portfolio.performance * 100).toFixed(2);
        return (
            <Fragment>
                <p>Tangent Portfolio: <span>{performance}% </span> at risk <span>{risk}% </span></p>
                <p>
                    {portfolio.allocations.map(this.renderAllocation)}
                </p>
            </Fragment>
        );

    };

    renderAllocation = (allocation: AllocationItem) => {
        const weight = (allocation.weight * 100).toFixed(2);
        return (
            <Fragment key={allocation.ticker}>
                <span>{allocation.ticker}</span> <span>{weight}% </span>
            </Fragment>
        );
    }
}