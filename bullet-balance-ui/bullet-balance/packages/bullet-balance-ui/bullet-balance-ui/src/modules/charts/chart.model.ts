
export type TDateChartPoint = {
    x: Date;
    y: number;
}

export type TNumberChartPoint = {
    x: number;
    y: number;
}

export type TChartPoint = TDateChartPoint | TNumberChartPoint;

export type TChartSeries = {
    points: TChartPoint[];
    color: string;
    name: string;
}

export type TChartData = {
    series: TChartSeries[];
    enableTime: boolean;
}


const LINEAR_SERIES: TChartSeries = {
    points: [{x: 1, y:2}, {x: 1.3, y:2.45}, {x: 2.34, y:3.34}],
    color: "#79C7E3",
    name: "Linear series"
};
export const LINEAR_CHART_FIXTURE: TChartData = {
    series: [LINEAR_SERIES],
    enableTime: false
};