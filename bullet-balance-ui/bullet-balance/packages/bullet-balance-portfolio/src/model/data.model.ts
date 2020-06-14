import { Optional } from "./optional.model";

export type TPoint = {
    x: number;
    y: number;
}

export type InstrumentFundamentals = {
    lotSize: number;
    priceToEarnings: Optional<number>;
    priceToBookValue: Optional<number>;
}

export const getGrahamRatio = (fundamentals: InstrumentFundamentals): Optional<number> => {
    return fundamentals.priceToEarnings.chain(pe => fundamentals.priceToBookValue.map(pb => (pb * pe)));
}

export const MAX_PE: number = 15;
export const MAX_PB: number = 1.5;

export type InstrumentStatistics = {
    fundamentals: InstrumentFundamentals;
    lastPrice: Optional<number>;
}

export type TPortfolio = {
    instruments: string[];
    weights: number[];
    risk: number;
    performance: number;
}

export type TTangentPortfolio = {
    points: TPoint[];
    lowest: TPortfolio;
    tangent: TPortfolio;
    statistics: InstrumentStatistics[];
    riskFree: TPoint;
}

export type TInstrument = {
    ticker: string;
    name: string;
}