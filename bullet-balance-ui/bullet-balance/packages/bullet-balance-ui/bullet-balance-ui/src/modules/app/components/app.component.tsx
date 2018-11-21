import * as React from 'react';
import { Component } from "react";
import { TangentPortfolioContainer } from "../../tangent-portfolio/containers/tangent-portfolio.container";

export interface AppComponentProps { compiler: string; framework: string; }

export class AppComponent extends Component<AppComponentProps, {}> {
    render() {
        return (
            <div>
                <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>
                <div>
                    <TangentPortfolioContainer width={500} height={400}/>
                </div>
            </div>
        );
    }
}