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
    tickers: string[];
}

export class TangentPortfolioComponent extends Component<TangentPortfolioComponentProps, TangentPortfolioComponentState> {

    private samples$ = new BehaviorSubject<number>(10000);
    private rate$ = new BehaviorSubject<number>(5.5);
    private tickers$ = new BehaviorSubject<string[]>([]);
    private sampleSubscription?: Subscription;
    private rateSubscription?: Subscription;
    private tickerSubscription?: Subscription;

    constructor(props: TangentPortfolioComponentProps) {
        super(props);
        this.state = {
            samplesCount: this.samples$.getValue(), 
            baseRate: this.rate$.getValue(), 
            baseRateText: `${this.rate$.getValue()}`,
            tickers: []
        };
        this.handleSampleChange = this.handleSampleChange.bind(this);
        this.handleRateChange = this.handleRateChange.bind(this);
        this.handleTickerChange = this.handleTickerChange.bind(this);
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
        this.tickerSubscription = this.tickers$
            .asObservable()
            .pipe(throttleTime(100))
            .pipe(observeOn(animationFrame))
            .subscribe(tickers => this.setState({tickers}));
    }

    componentWillUnmount() {
        if (this.sampleSubscription) {
            this.sampleSubscription.unsubscribe();
        }
        if (this.rateSubscription) {
            this.rateSubscription.unsubscribe();
        }
        if (this.tickerSubscription) {
            this.tickerSubscription.unsubscribe();
        }
    }

    render() {
        return (
            <form className="container-fluid">
                <div className="row">
                <div className="col-8">
                            <div className="form-group">
                                <label htmlFor="tickers">Portfolio</label>
                                <input className="form-control" id="tickers" onChange={this.handleTickerChange} />
                            </div>                            
                  </div>
                  </div>
                    <div className="row">
                        <div className="col-4">
                            <div className="form-group">
                                <label htmlFor="baseRate">Risk free rate</label>
                                <input className="form-control" id="baseRate" value={this.state.baseRateText}  onChange={this.handleRateChange} />
                            </div>                            
                        </div>
                        <div className="col-4">
                            <div className="form-group">
                                <label htmlFor="samples">Samples count</label>
                                <input className="form-control" id="samples" value={this.state.samplesCount}  onChange={this.handleSampleChange} />
                            </div>                            
                        </div>
                    </div>
                <div className="row">
                    <div className="col-12">
                        <TangentPortfolioChartContainer 
                                width={500} 
                                height={400} 
                                tickers={this.state.tickers}
                                samplesCount={this.state.samplesCount} 
                                baseRate={this.state.baseRate}/>
                    </div>
                </div>
            </form>
        );
    }

    handleSampleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const count = parseInt(event.currentTarget.value, 10);
        if (count && !isNaN(count)) {
            this.samples$.next(count);
        }
    }

    handleTickerChange = (event: ChangeEvent<HTMLInputElement>) => {
        const tickerText: string = event.currentTarget.value;
        const tokens = tickerText.split(',').map(item => item.trim()).filter(item => item.length > 0);
        this.tickers$.next(tokens);
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