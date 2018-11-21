import * as React from 'react';
import { Component } from "react";
import {XYChartComponent} from "../../charts/chart.component";
import { LINEAR_CHART_FIXTURE } from "../../charts/chart.model";

export interface AppComponentProps { compiler: string; framework: string; }

export class AppComponent extends Component<AppComponentProps, {}> {
    render() {
        return ( <div>
                    <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>
                    <div>
                        <XYChartComponent width={500} height={400} chartData={LINEAR_CHART_FIXTURE} />
                    </div>
                </div>
        );
    }
}