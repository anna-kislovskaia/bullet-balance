import * as React from 'react';
import { useState, ChangeEvent } from 'react';
import { TTangentPortfolio, TPortfolio, InstrumentStatistics, getGrahamRatio, MAX_PE, MAX_PB } from '../../model/data.model';
import { NullableComponent } from '../common/nullable-component';
import { Optional } from '../../model/optional.model';
import { PortfolioAllocationService } from '../../services/portfolio-allocation.service';

export type TangentPortfolioAllocationProps = {
    data: TTangentPortfolio;
    investments: number;
}

export function TangentPortfolioAllocationComponent(props: TangentPortfolioAllocationProps) {
    const [investmentsText, setInvestmentsText] = useState(`${props.investments}`);
  
    return (
        <>
        <div className="row mt-16">
            <div className="col-4">
                <div className="form-group">
                    <label htmlFor="startDate">Investments</label>
                    <input className="form-control" id="baseRate" value={investmentsText}  onChange={event => updateAmount(event, setInvestmentsText)} />
                </div>                            
            </div>        
        </div>        
        {renderAllocations(props.data, props.investments)}
        </>
    );
  }

  const updateAmount = (event: ChangeEvent<HTMLInputElement>, setValue: (value?: string) => any) => {
    const value = event.currentTarget.value;
    const amount = Number.parseFloat(value);
    const investments = amount && !isNaN(amount) ? amount : 0;
    PortfolioAllocationService.setInvestments(investments);
    setValue(value);
  }

  const renderAllocations = (data: TTangentPortfolio, totalInvestments: number): JSX.Element => {
    const tickers = data.tangent.instruments;
    const headers = ['Tangent', 'Lowest Risk', 'Last', 'P/E', 'P/Book', 'Graham Ratio', 
      'Tangent Equities (Lots)', 'Investments', 'Lowest Equities(Lots)', 'Investments'];
    return (
        <div className="row mt-16">
        <div className="col-8">
        <table className="table table-hover">
            <thead>
                <tr>
                    <th scope="col">Ticker</th>
                    {headers.map((header, idx) => (<th key={`header-${idx}`} scope="col">{header}</th>))}
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
     const tangentCount = getEquitiesCount(data.tangent.weights[index], totalInvestments, lastPrice);
     const lowestCount = getEquitiesCount(data.lowest.weights[index], totalInvestments, lastPrice);
     const tangentCountStr = formatEquitiesCount(statistics, tangentCount);
     const lowestCountStr = formatEquitiesCount(statistics, lowestCount);
     const grahamRatio = getGrahamRatio(statistics.fundamentals); 
     const tangentInvestments = getInstrumentInvestments(statistics, tangentCount, lastPrice);  
     const lowestInvestments = getInstrumentInvestments(statistics, lowestCount, lastPrice);  
     return (
        <tr key={ticker}>
            <td scope="row">{ticker}</td>
            {portfolios.map(portfolio => (portfolio.weights[index] * 100).toFixed(2))
                .map((weight, i) => <td key={`wieght-${i}`}>{weight}% </td>)}
            <td><NullableComponent value={lastPrice}/></td>     
            <td>{renderRatio(statistics.fundamentals.priceToEarnings, MAX_PE)}</td>     
            <td>{renderRatio(statistics.fundamentals.priceToBookValue, MAX_PB)}</td>     
            <td>{renderRatio(grahamRatio, MAX_PB * MAX_PE)}</td>     
            <td><NullableComponent value={tangentCountStr}/></td>     
            <td><NullableComponent value={tangentInvestments}/></td>     
            <td><NullableComponent value={lowestCountStr}/></td>     
            <td><NullableComponent value={lowestInvestments}/></td>     
        </tr>
    );
}

const renderRatio = (value: Optional<number>, maximum: number) => {
    const className: string = value
    .map(val => { return (val > maximum ? "text-danger" : "text-success") as string}).getOrElse("");
    return<span className={className}><NullableComponent value={value.map(val => val.toFixed(2))}/></span>;
}

const formatEquitiesCount = (statistics: InstrumentStatistics, equitiesCount: Optional<number>) => {
    const lotSize = statistics.fundamentals.lotSize;
    return equitiesCount.map(count => `${count} (${Math.floor(count / lotSize)})`);
}

const getEquitiesCount = (weight: number, investments: number, lastPrice: Optional<number>): Optional<number> => { 
    return lastPrice.map(price => Math.floor((investments * weight) / price));
}

const getInstrumentInvestments = (statistics: InstrumentStatistics, count: Optional<number>, lastPrice: Optional<number>) => {
    const lotSize = statistics.fundamentals.lotSize;
    return count.chain(cnt => {
        const normilizedByLots = Math.floor(cnt / lotSize) * lotSize; 
        return lastPrice.map(price => Math.ceil(price * normilizedByLots));
    });
}
