import * as React from 'react';
import { Component } from "react";
import './app.styles.scss';
import { TangentPortfolioContainer } from '../tangent-portfolio/tangent-portfolio.container';

export interface AppComponentProps { compiler: string; framework: string; }

export class AppComponent extends Component<AppComponentProps, {}> {
    render() {
        return (
            <div className="container-fluid">
                <div className="row">
                    <h1>Markowitz Tangent Portfolio Calculator</h1>
                </div>
                <div className="row">
                    <TangentPortfolioContainer />
                </div>    
            </div>
        );
    }
}