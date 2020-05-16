import * as React from 'react';
import { useState, ChangeEvent } from 'react';
import { PortfolioData } from "./tangent-portoflio-chart.component";
import { TPortfolio } from '../../model/data.model';

const initialInvestments = 100000;

export type TangentPortfolioAllocationProps = {
    data: PortfolioData;
}

export function TangentPortfolioAllocationComponent(props: TangentPortfolioAllocationProps) {
    const [investmentsText, setInvestmentsText] = useState(`${initialInvestments}`);
    const [investments, setInvestments] = useState(initialInvestments);
  
    return (
        <>
        <div className="row mt-16">
            <div className="col-4">
                <div className="form-group">
                    <label htmlFor="startDate">Investments</label>
                    <input className="form-control" id="baseRate" value={investmentsText}  onChange={event => updateAmount(event, setInvestmentsText, setInvestments)} />
                </div>                            
            </div>        
        </div>        
        {renderAllocations(props.data, investments)}
        </>
    );
  }

  const updateAmount = (event: ChangeEvent<HTMLInputElement>, setValue: (value?: string) => any, setInvestments: (value: number) => any) => {
    const value = event.currentTarget.value;
    const amount = Number.parseFloat(value);
    const investments = amount && !isNaN(amount) ? amount : 0;
    setInvestments(investments);
    setValue(value);
  }

  const renderAllocations = (data: PortfolioData, totalInvestments: number): JSX.Element => {
    const portfolios = [data.tangent, data.lowest];
    const tickers = portfolios[0].instruments;
    const headers = ['Tangent', 'Lowest Risk', 'Last', 'Equities Count'];
    return (
        <div className="row mt-16">
        <div className="col-8">
        <table className="table table-hover">
            <thead>
                <tr>
                    <th scope="col">Ticker</th>
                    {headers.map(header => (<th key={header} scope="col">{header}</th>))}
                </tr>
            </thead>
            <tbody>
                {tickers.map((ticker, index) => renderAllocation(ticker, index, portfolios, totalInvestments))}
            </tbody>
        </table>    
        </div>
        </div>
    );

};

 const renderAllocation = (ticker: string, index: number, portfolios: TPortfolio[], totalInvestments: number) => {
     const tangent = portfolios[0];
     const lastPrice = tangent.lastPrices[index];
     const wieght = tangent.weights[index];
     const equitiesCount = Math.round((totalInvestments * wieght) / lastPrice);
    return (
        <tr key={ticker}>
            <td scope="row">{ticker}</td>
            {portfolios.map(portfolio => (portfolio.weights[index] * 100).toFixed(2))
                .map((weight, i) => <td key={`wieght-${i}`}>{weight}% </td>)}
            <td>{lastPrice}</td>     
            <td>{equitiesCount}</td>     
        </tr>
    );
}
