import * as React from 'react';
import { Component, Fragment} from "react";
import { XYChartComponent, TChartData, ChartLegend, TPlotLegend } from "@bullet-balance/components";
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
        const { chartData, allocation, width, height } = this.props;
        return (
            <div>
                {taskRenderer(allocation, (portfolio) => {
                    const risk = (portfolio.risk * 100).toFixed(2);
                    const performance = (portfolio.performance * 100).toFixed(2);
                    return <p>Tangent Portfolio: <span>{performance}% </span> at risk <span>{risk}% </span></p>
                })}
                {taskRenderer(chartData, (result) => <XYChartComponent chartData={result} width={width} height={height}/>)}
                {allocation.getNullable() && this.renderAllocations(allocation.getNullable())}
            </div>
        );
    }

      renderAllocations = (portfolio: PortfolioAllocation) => {
        if (portfolio.allocations.length === 0) {
            return null;
        }
        return (
            <table>
                <tbody>
                    <tr>
                        <th>Ticker</th>
                        <th>Fraction</th>
                    </tr>
                    {portfolio.allocations.map(this.renderAllocation)}
                </tbody>
            </table>    
        );

    };

    renderAllocation = (allocation: AllocationItem) => {
        const weight = (allocation.weight * 100).toFixed(2);
        return (
            <tr key={allocation.ticker}>
                <td>{allocation.ticker}</td> 
                <td>{weight}% </td>
            </tr>
        );
    }
}