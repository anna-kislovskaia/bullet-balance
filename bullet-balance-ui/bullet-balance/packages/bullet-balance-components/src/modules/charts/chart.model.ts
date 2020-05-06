import { type } from "os";

export enum Shape {
    line, square, circle
}

export type TDateChartPoint = {
    x: Date;
    y: number;
}

export type TNumberChartPoint = {
    x: number;
    y: number;
}

export type TRangeChartPoint = {
    x: number;
    y: number[];
}

export type TChartPoint = TDateChartPoint | TNumberChartPoint | TRangeChartPoint;

export enum ChartSeriesType {
  LINEAR = 1,
  RANGE = 2
}

export type TChartSeries = {
    points: TChartPoint[];
    key: string;
    color: string;
    name: string;
    type: ChartSeriesType;
}

export type TChartData = {
    series: TChartSeries[];
    enableTime: boolean;
}


const LINEAR_SERIES: TChartSeries = {
    points: [{x: 1, y:2}, {x: 1.3, y:2.45}, {x: 2.34, y:3.34}],
    color: "#79C7E3",
    key: "series1",
    name: "Linear series",
    type: ChartSeriesType.LINEAR
};
export const LINEAR_CHART_FIXTURE: TChartData = {
    series: [LINEAR_SERIES],
    enableTime: false
};