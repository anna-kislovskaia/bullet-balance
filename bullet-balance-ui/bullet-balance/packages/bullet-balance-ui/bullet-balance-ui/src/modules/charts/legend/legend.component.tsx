import * as React from 'react';
import { Component, Fragment } from 'react';
import {TPlotLegend} from "./legend.model";
import {Shape} from "../chart.model";

export type ChartLegendProps = {
    items: TPlotLegend[];
}

export class ChartLegend extends Component<ChartLegendProps, {}> {
    render() {
        const { items } = this.props;
        return (
            <p>
                {items.map(item => {
                    return (
                        <span key={`legend-${item.name}`}>
                            {this.renderPlotMarker(item)}
                            <span>{item.name} </span>
                        </span>
                    );
                })}
            </p>

        );
    }

    renderPlotMarker = (item: TPlotLegend) => {
        const style = {stroke: item.color, strokeWidth : 1, fill: item.color};
        switch(item.shape) {
            case Shape.line:
                return (
                    <svg height="10" width="20">
                        <line x1="0" y1="5" x2="15" y2="5" style={style} />
                    </svg>
                );
            case Shape.square:
                return (
                    <svg height="10" width="20">
                        <rect width="10" height="10" style={style} />
                    </svg>
                );
            case Shape.circle:
                return (
                    <svg height="10" width="20">
                        <circle cx="50" cy="50" r="10" style={style} />
                    </svg>
                );

        }
    }
}
