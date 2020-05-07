import * as React from 'react';
import {Component} from "react";
import {XYChartComponent, TChartData } from "@bullet-balance/components";
import {Task} from "../../../utils/task.model";
import {LoadingIndicatorComponent} from '../../loading-indicator/loading-indicator.component';
import {TPortfolio} from '../../../model/data.model';

export type PortfolioData = {
    tangent: TPortfolio;
    lowest: TPortfolio;
}

export interface TangentPortfolioChartProps {
    chartData: Task<TChartData>;
    portfolios: Task<PortfolioData>;
    width: number;
    height: number;
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
                    return <p>Tangent Portfolio: <span>{performance}% </span> at risk <span>{risk}% </span></p>
                })}
                {taskRenderer(chartData, (result) => <XYChartComponent chartData={result} width={width} height={height}/>)}
                {portfolios.getNullable() && this.renderAllocations(portfolios.getNullable())}
            </div>
        );
    }

      renderAllocations = (data: PortfolioData) => {
        const portfolios = [data.tangent, data.lowest];
        const tickers = portfolios[0].instruments;
        const headers = ['Tangent', 'Lowest Risk'];
        return (
            <table>
                <tbody>
                    <tr>
                        <th>Ticker</th>
                        {headers.map(header => (<th>{header}</th>))}
                    </tr>
                    {tickers.map((ticker, index) => this.renderAllocation(ticker, index, portfolios))}
                </tbody>
            </table>    
        );

    };

    renderAllocation = (ticker: string, index: number, portfolios: TPortfolio[]) => {
        return (
            <tr key={ticker}>
                <td>{ticker}</td>
                {portfolios.map(portfolio => (portfolio.weights[index] * 100).toFixed(2))
                    .map(weight => <td>{weight}% </td>)} 
            </tr>
        );
    }
}