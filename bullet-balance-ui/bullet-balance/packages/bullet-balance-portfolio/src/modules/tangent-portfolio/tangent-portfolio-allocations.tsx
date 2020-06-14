import * as React from 'react';
import { useState, ChangeEvent } from 'react';
import { TTangentPortfolio, TPortfolio, InstrumentStatistics, getGrahamRatio, MAX_PE, MAX_PB } from '../../model/data.model';
import { NullableComponent } from '../common/nullable-component';
import { Optional } from '../../model/optional.model';

const initialInvestments = 100000;

export type TangentPortfolioAllocationProps = {
    data: TTangentPortfolio;
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

  const renderAllocations = (data: TTangentPortfolio, totalInvestments: number): JSX.Element => {
    const tickers = data.tangent.instruments;
    const headers = ['Tangent', 'Lowest Risk', 'Last', 'P/E', 'P/Book', 'Graham Ratio', 'Tangent Equities Count(Lots)', 'Lowest Equities Count(Lots)'];
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
                {tickers.map((ticker, index) => renderAllocation(ticker, index, data, totalInvestments))}
            </tbody>
        </table>    
        </div>
        </div>
    );

};

 const renderAllocation = (ticker: string, index: number, data: TTangentPortfolio, totalInvestments: number) => {
     const portfolios = [data.tangent, data.lowest]
     const statistics = data.statistics[index];
     const lastPrice = statistics.lastPrice;
     const equitiesCount = getEquitiesCount(statistics, data.tangent.weights[index], totalInvestments, lastPrice);
     const lowestCount = getEquitiesCount(statistics, data.lowest.weights[index], totalInvestments, lastPrice);
     const grahamRatio = getGrahamRatio(statistics.fundamentals); 
    return (
        <tr key={ticker}>
            <td scope="row">{ticker}</td>
            {portfolios.map(portfolio => (portfolio.weights[index] * 100).toFixed(2))
                .map((weight, i) => <td key={`wieght-${i}`}>{weight}% </td>)}
            <td><NullableComponent value={lastPrice}/></td>     
            <td>{renderRatio(statistics.fundamentals.priceToEarnings, MAX_PE)}</td>     
            <td>{renderRatio(statistics.fundamentals.priceToBookValue, MAX_PB)}</td>     
            <td>{renderRatio(grahamRatio, MAX_PB * MAX_PE)}</td>     
            <td><NullableComponent value={equitiesCount}/></td>     
            <td><NullableComponent value={lowestCount}/></td>     
        </tr>
    );
}

const renderRatio =(value: Optional<number>, maximum: number) => {
    const className: string = value
    .map(val => { return (val > maximum ? "text-danger" : "text-success") as string}).getOrElse("");
    return<span className={className}><NullableComponent value={value.map(val => val.toFixed(2))}/></span>;
}

const getEquitiesCount = (statistics: InstrumentStatistics, weight: number, investments: number, lastPrice: Optional<number>) => {
    const equitiesCount = lastPrice.map(price => Math.floor((investments * weight) / price));
    const lotSize = statistics.fundamentals.lotSize;
    return equitiesCount.map(count => `${count} (${Math.floor(count / lotSize)})`);
}
