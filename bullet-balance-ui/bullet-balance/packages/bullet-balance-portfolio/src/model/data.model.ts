
export type TPoint = {
    x: number;
    y: number;
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
    riskFree: TPoint;
}

export type TInstrument = {
    ticker: string;
    name: string;
}