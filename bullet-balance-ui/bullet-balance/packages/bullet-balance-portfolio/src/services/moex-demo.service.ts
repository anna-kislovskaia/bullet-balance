import { REMOTE_API } from "../utils/task.utils";
import { TPortfolio, TTangentPortfolio, TInstrument, InstrumentStatistics } from "../model/data.model";
import { Task, TaskUtils} from "../utils/task.model";
import {map, observeOn} from "rxjs/internal/operators";
import { of } from 'rxjs';
import { Observable } from "rxjs/index";
import {async} from "rxjs/internal/scheduler/async";
import {format} from "date-fns";
import { fromNullable } from "../model/optional.model";

export namespace MoexDemoService {
    const illegalArguments = of(TaskUtils.failure<TTangentPortfolio>(new Error("Invalid arguments")));
    const DATE_FORMAT = 'yyyyMMdd';

    export const getInstruments = (): Observable<Task<TInstrument[]>> => {
        const url = 'api/demo/moex/instruments';
        return REMOTE_API.get<TInstrument[]>(url)
            .pipe(observeOn(async));
    };


    export const getMoexSampleCurve = (tickers: string[], fromDate?: Date, toDate?: Date, baseRate?: number, samplesCount?: number): Observable<Task<TTangentPortfolio>> => {
        if (tickers.length === 0 || !fromDate || !toDate) {
            return illegalArguments;
        }
        const fromDateStr = format(fromDate, DATE_FORMAT);
        const toDateStr = format(toDate, DATE_FORMAT);
        let url = `api/demo/moex/portfolio?sampleCount=${samplesCount}&tickers=${tickers.join(',')}&fromDate=${fromDateStr}&toDate=${toDateStr}`;
        if (baseRate) {
            url += `&riskFreeRate=${baseRate}`;
        }
        return REMOTE_API.get<any>(url)
            .pipe(observeOn(async))
            .pipe(map(task => task.map<TTangentPortfolio>(parseTangentPortfolio)));
    };

    const parseTangentPortfolio = (raw: any): TTangentPortfolio => {
        const tangent: TPortfolio = {
            instruments: raw.instruments,
            weights: raw.tangentPortfolioAllocation.weights,
            risk: raw.tangentPortfolioAllocation.weighthedRisk, 
            performance: raw.tangentPortfolioAllocation.weightedReturn 
        };
        const lowest: TPortfolio = {
            instruments: raw.instruments,
            weights: raw.lowestRiskAllocation.weights,
            risk: raw.lowestRiskAllocation.weighthedRisk, 
            performance: raw.lowestRiskAllocation.weightedReturn
        };
        const statistics: InstrumentStatistics[] = (raw.statistics as any[]).map(value => {
            return {
                lastPrice: fromNullable(value.lastPrice),
                fundamentals: {
                    lotSize: value.fundamentals.lotSize,
                    priceToEarnings: fromNullable(value.fundamentals.priceToEarnings),
                    priceToBookValue: fromNullable(value.fundamentals.priceToBookValue)
                }
            }
        });
        return {
            points: raw.chartPlot.points,
            lowest,
            tangent,
            statistics,
            riskFree: {x: 0, y: raw.riskFreeRate}
        }
    }
}