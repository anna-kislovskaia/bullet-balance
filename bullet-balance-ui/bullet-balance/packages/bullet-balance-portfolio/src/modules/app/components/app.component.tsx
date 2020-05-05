import * as React from 'react';
import { Component } from "react";
import {TangentPortfolioComponent} from "../../tangent-portfolio/components/tangent-portfolio.component";

export interface AppComponentProps { compiler: string; framework: string; }

export class AppComponent extends Component<AppComponentProps, {}> {
    render() {
        return (
            <div>
                <div>
                    <TangentPortfolioComponent />
                </div>
            </div>
        );
    }
}