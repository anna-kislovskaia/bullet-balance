
export type TPoint = {
    x: number;
    y: number;
}

export type TPortfolio = {
    instruments: string[];
    weights: number[];
}

export type TTangentPortfolio = {
    portfolio: TPortfolio;
    points: TPoint[];
    lowest: TPoint;
    tangent: TPoint;
    riskFree: TPoint;
}