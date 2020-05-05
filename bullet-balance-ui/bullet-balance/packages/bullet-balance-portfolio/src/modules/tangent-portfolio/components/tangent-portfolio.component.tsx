import * as React from 'react';
import { Component, ChangeEvent } from "react";
import {TangentPortfolioChartContainer} from "../containers/tangent-portfolio-chart.container";
import {BehaviorSubject, Subscription} from "rxjs/index";
import { observeOn, throttleTime} from "rxjs/internal/operators";
import {animationFrame} from "rxjs/internal/scheduler/animationFrame";

export interface TangentPortfolioComponentProps {}

type TangentPortfolioComponentState = {
    samplesCount: number;
}

export class TangentPortfolioComponent extends Component<TangentPortfolioComponentProps, TangentPortfolioComponentState> {

    private samples$ = new BehaviorSubject<number>(10000);
    private resultsSubscription?: Subscription;

    constructor(props: TangentPortfolioComponentProps) {
        super(props);
        this.state = {samplesCount: 10000};
        this.handleChange = this.handleChange.bind(this);
    }

    componentWillMount() {
        this.resultsSubscription = this.samples$
            .asObservable()
            .pipe(throttleTime(100))
            .pipe(observeOn(animationFrame))
            .subscribe(count => this.setState({samplesCount: count}));
    }

    componentWillUnmount() {
        if (this.resultsSubscription) {
            this.resultsSubscription.unsubscribe();
        }
    }

    render() {
        return (
            <table>
                <tbody>
                    <tr>
                        <td>
                            <input value={this.state.samplesCount} onChange={this.handleChange}/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <TangentPortfolioChartContainer width={500} height={400} samplesCount={this.state.samplesCount}/>
                        </td>
                    </tr>
                </tbody>
            </table>
        );
    }

    handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const count = parseInt(event.currentTarget.value, 10);
        if (count && !isNaN(count)) {
            this.samples$.next(count);
        }
    }
}