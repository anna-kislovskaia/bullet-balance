import { ComponentClass, Component } from 'react';
import { RxProperties, Omit, rxComponentFactory} from "../../../utils/rx.utils";
import { of } from 'rxjs';
import {TangentPortfolioComponent, TangentPortfolioComponentProps} from "../tangent-portoflio.component";
import { LINEAR_CHART_FIXTURE, Shape} from "../../charts/chart.model";

type DefaultProperties = 'width' | 'height';

const props$: RxProperties<DefaultProperties, TangentPortfolioComponentProps> = () => {
    const legendData = LINEAR_CHART_FIXTURE.series.map(serie => {
        return {shape: Shape.line, ...serie};
    });
    const result: Omit<TangentPortfolioComponentProps, DefaultProperties> = {
        chartData: LINEAR_CHART_FIXTURE,
        legend: legendData
    };
    return of(result);
};

export const TangentPortfolioContainer = rxComponentFactory<DefaultProperties, TangentPortfolioComponentProps>(props$)(TangentPortfolioComponent);
