import * as React from 'react';
import { Component, ChangeEvent } from "react";
import {TangentPortfolioChartContainer} from "./tangent-portfolio-chart.container";
import {BehaviorSubject, Subscription} from "rxjs/index";
import { observeOn, throttleTime} from "rxjs/internal/operators";
import {animationFrame} from "rxjs/internal/scheduler/animationFrame";
import { TInstrument } from '../../model/data.model';
import { Task } from '../../utils/task.model';
import Select, { ValueType } from 'react-select';
import { addDays } from 'date-fns';
import DatePicker from "react-datepicker";

export interface TangentPortfolioComponentProps {
    instruments: Task<TInstrument[]>;
}

type TangentPortfolioComponentState = {
    samplesCount: number;
    baseRate: number;
    baseRateText: string;
    startDate?: Date;
    endDate?: Date;
    tickers: string[];
}

type SelectionOption = {
    value: string;
    label: string;
}

const DATE_FORMAT = 'dd.MM.yyyy';

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
            startDate: addDays(new Date(), -90),
            endDate: new Date(),
            tickers: []
        };
        this.handleSampleChange = this.handleSampleChange.bind(this);
        this.handleRateChange = this.handleRateChange.bind(this);
        this.handleTickerChange = this.handleTickerChange.bind(this);
    }

    componentDidMount() {
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
        const selectOptions: SelectionOption[] = this.props.instruments
        .map(instruments => instruments.map(instrument => 
            {return {value: instrument.ticker, label: `${instrument.ticker} (${instrument.name})`}})).optional().getOrElse([]);
        return (
            <form className="container-fluid">
                <div className="row">
                    <div className="col-8">
                        <div className="form-group">
                            <label htmlFor="tickers">Portfolio</label>
                            <Select
                                    isMulti
                                    id="tickers"
                                    options={selectOptions}
                                    className="basic-multi-select form-control"
                                    classNamePrefix="select"
                                    onChange={this.handleTickerChange}
                            />
                        </div>                            
                    </div>
                </div>
                <div className="row">
                        <div className="col-4">
                            <div className="form-group">
                                <label htmlFor="startDate">Start Date</label>
                                <DatePicker 
                                    className="form-control" 
                                    id="startDate" 
                                    selected={this.state.startDate} 
                                    dateFormat={DATE_FORMAT} 
                                    onChange={(date) => this.setState({startDate: date})} 
                                    />
                            </div>                            
                        </div>
                        <div className="col-4">
                            <div className="form-group">
                                <label htmlFor="endDate">End Date</label>
                                <DatePicker 
                                    className="form-control" 
                                    id="startDate" 
                                    dateFormat={DATE_FORMAT} 
                                    selected={this.state.endDate}  
                                    onChange={(date) => this.setState({endDate: date})} 
                                    />
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
                <div className="row mt-16">
                    <div className="col-12">
                        <TangentPortfolioChartContainer 
                                width={500} 
                                height={400} 
                                startDate={this.state.startDate}
                                endDate={this.state.endDate}
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

    handleTickerChange = (value: ValueType<SelectionOption>) => {
        if (!value) {
            this.tickers$.next([]);
        } else {
            const options = value as SelectionOption[];
            const tickers = options.map(option => option.value);
            this.tickers$.next(tickers);
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