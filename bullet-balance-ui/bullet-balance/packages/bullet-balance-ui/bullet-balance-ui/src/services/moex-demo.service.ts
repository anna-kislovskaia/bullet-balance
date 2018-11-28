import { REMOTE_API } from "../utils/task.utils";
import { TPoint, TPortfolio, TTangentPortfolio } from "../model/data.model";
import { Task } from "../utils/task.model";
import {map, observeOn} from "rxjs/internal/operators";
import { Observable } from "rxjs/index";
import {async} from "rxjs/internal/scheduler/async";

export namespace MoexDemoService {
    export const getMoexSampleCurve = (samplesCount: number): Observable<Task<TTangentPortfolio>> => {
        return REMOTE_API.get<any>('api/demo/moex/sample?samplesCount=' + samplesCount)
            .pipe(observeOn(async))
            .pipe(map(task => task.map<TTangentPortfolio>(parseTangentPortfolio)));
    };

    const parseTangentPortfolio = (raw: any): TTangentPortfolio => {
        const portfolio: TPortfolio = {
            instruments: raw.instruments,
            weights: raw.tangentPortfolioAllocation.weights
        };
        const lowest: TPoint = {x: raw.lowestRiskAllocation.weighthedRisk, y: raw.lowestRiskAllocation.weightedReturn};
        const tangent: TPoint = {x: raw.tangentPortfolioAllocation.weighthedRisk, y: raw.tangentPortfolioAllocation.weightedReturn};
        return {
            portfolio,
            points: raw.chartPlot.points,
            lowest,
            tangent,
            riskFree: {x: 0, y: raw.riskFreeRate}
        }
    }
}