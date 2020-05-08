import * as React from 'react';
import { Component } from "react";
import {TangentPortfolioComponent} from "../tangent-portfolio/components/tangent-portfolio.component";
import './app.styles.scss';

export interface AppComponentProps { compiler: string; framework: string; }

export class AppComponent extends Component<AppComponentProps, {}> {
    render() {
        return (
            <div className="container-fluid">
                <div className="row">
                    <h1>Markowitz Tangent Portfolio Calculator</h1>
                </div>
                <div className="row">
                    <TangentPortfolioComponent />
                </div>    
            </div>
        );
    }
}