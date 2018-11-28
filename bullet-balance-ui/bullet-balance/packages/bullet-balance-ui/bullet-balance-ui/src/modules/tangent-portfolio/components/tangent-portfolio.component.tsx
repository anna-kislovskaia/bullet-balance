import * as React from 'react';
import { Component, ChangeEvent } from "react";
import {TangentPortfolioChartContainer} from "../containers/tangent-portfolio-chart.container";

export interface TangentPortfolioComponentProps {}

type TangentPortfolioComponentState = {
    samplesCount: number;
}

export class TangentPortfolioComponent extends Component<TangentPortfolioComponentProps, TangentPortfolioComponentState> {

    constructor(props: TangentPortfolioComponentProps) {
        super(props);
        this.state = {samplesCount: 10000};
        this.handleChange = this.handleChange.bind(this);
    }

    render() {
        return (
            <table>
                <tr>
                    <input value={this.state.samplesCount} onChange={this.handleChange}/>
                </tr>
                <tr>
                    <td>
                        <TangentPortfolioChartContainer width={500} height={400} samplesCount={this.state.samplesCount}/>
                    </td>
                </tr>
            </table>
        );
    }

    handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const count = parseInt(event.currentTarget.value, 10);
        this.setState({samplesCount: count});
    }
}