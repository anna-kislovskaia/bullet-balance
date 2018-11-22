import { REMOTE_API } from "../utils/task.utils";
import { TPoint, TPortfolio, TTangentPortfolio } from "../model/data.model";
import { Task } from "../utils/task.model";
import { map } from "rxjs/internal/operators";
import { Observable } from "rxjs/index";

export namespace MoexDemoService {
    export const getMoexSampleCurve = (): Observable<Task<TTangentPortfolio>> => {
        return REMOTE_API.get<any>('demo/moex/sample')
            .pipe(map(task => task.map<TTangentPortfolio>(parseTangentPortfolio)));
    };

    const parseTangentPortfolio = (raw: any): TTangentPortfolio => {
        const portfolio: TPortfolio = {
            instruments: raw.instruments,
            weights: raw.tangentPortfolioAllocation.weights
        };
        const lowest: TPoint = {x: raw.lowestRiskAllocation.weigthedRisk, y: raw.lowestRiskAllocation.weigthedReturn};
        const tangent: TPoint = {x: raw.tangentPortfolioAllocation.weigthedRisk, y: raw.tangentPortfolioAllocation.weigthedReturn};
        return {
            portfolio,
            points: raw.chartPlot.points,
            lowest,
            tangent,
            riskFree: {x: 0, y: 0.25}
        }
    }
}