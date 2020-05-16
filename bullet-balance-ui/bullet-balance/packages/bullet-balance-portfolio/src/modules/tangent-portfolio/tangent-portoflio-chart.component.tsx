import * as React from 'react';
import {Component} from "react";
import {XYChartComponent, TChartData } from "@bullet-balance/components";
import {Task} from "../../utils/task.model";
import {LoadingIndicatorComponent} from '../loading-indicator/loading-indicator.component';
import {TPortfolio} from '../../model/data.model';
import { TangentPortfolioAllocationComponent } from './tangent-portfolio-allocations';

export type PortfolioData = {
    tangent: TPortfolio;
    lowest: TPortfolio;
}

export interface TangentPortfolioChartProps {
    chartData: Task<TChartData>;
    portfolios: Task<PortfolioData>;
    width: number;
    height: number;
    startDate?: Date;
    endDate?: Date; 
    samplesCount: number;
    baseRate: number;
    tickers: string[];
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
        const { chartData, portfolios, width, height } = this.props;
        return (
            <div>
                {taskRenderer(portfolios, (data) => {
                    const risk = (data.tangent.risk * 100).toFixed(2);
                    const performance = (data.tangent.performance * 100).toFixed(2);
                    return <h3>Tangent Portfolio: <span>{performance}% </span> at risk <span>{risk}% </span></h3>
                })}
                {taskRenderer(chartData, (result) => <XYChartComponent chartData={result} width={width} height={height}/>)}
                {portfolios.getNullable() && <TangentPortfolioAllocationComponent data={portfolios.getNullable()} />}
            </div>
        );
    }

}