import * as React from 'react';
import { Component, ChangeEvent } from "react";
import {TangentPortfolioChartContainer} from "../containers/tangent-portfolio-chart.container";
import {BehaviorSubject, Subscription} from "rxjs/index";
import { observeOn, throttleTime} from "rxjs/internal/operators";
import {animationFrame} from "rxjs/internal/scheduler/animationFrame";

export interface TangentPortfolioComponentProps {}

type TangentPortfolioComponentState = {
    samplesCount: number;
    baseRate: number;
    baseRateText: string;
}

export class TangentPortfolioComponent extends Component<TangentPortfolioComponentProps, TangentPortfolioComponentState> {

    private samples$ = new BehaviorSubject<number>(10000);
    private rate$ = new BehaviorSubject<number>(5.5);
    private sampleSubscription?: Subscription;
    private rateSubscription?: Subscription;

    constructor(props: TangentPortfolioComponentProps) {
        super(props);
        this.state = {
            samplesCount: this.samples$.getValue(), 
            baseRate: this.rate$.getValue(), 
            baseRateText: `${this.rate$.getValue()}`
        };
        this.handleSampleChange = this.handleSampleChange.bind(this);
        this.handleRateChange = this.handleRateChange.bind(this);
    }

    componentWillMount() {
        this.sampleSubscription = this.samples$
            .asObservable()
            .pipe(throttleTime(100))
            .pipe(observeOn(animationFrame))
            .subscribe(count => this.setState({samplesCount: count}));
        this.rateSubscription = this.rate$
            .asObservable()
            .pipe(throttleTime(100))
            .pipe(observeOn(animationFrame))
            .subscribe(rate => this.setState({baseRate: rate}));
    }

    componentWillUnmount() {
        if (this.sampleSubscription) {
            this.sampleSubscription.unsubscribe();
        }
        if (this.rateSubscription) {
            this.rateSubscription.unsubscribe();
        }
    }

    render() {
        return (
            <table>
                <tbody>
                    <tr>
                        <td>
                            Sample count: <input value={this.state.samplesCount} onChange={this.handleSampleChange}/>
                        </td>
                    </tr>
                    <tr>    
                        <td>
                            Risk free rate: <input value={this.state.baseRateText} onChange={this.handleRateChange}/>%
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <TangentPortfolioChartContainer 
                                width={500} 
                                height={400} 
                                samplesCount={this.state.samplesCount} 
                                baseRate={this.state.baseRate}/>
                        </td>
                    </tr>
                </tbody>
            </table>
        );
    }

    handleSampleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const count = parseInt(event.currentTarget.value, 10);
        if (count && !isNaN(count)) {
            this.samples$.next(count);
        }
    }

    handleRateChange = (event: ChangeEvent<HTMLInputElement>) => {
        const baseRateText = event.currentTarget.value;
        const rate = Number.parseFloat(event.currentTarget.value);
        if (rate && !isNaN(rate)) {
            this.rate$.next(rate);
        }
        this.setState({baseRateText});
    }
}