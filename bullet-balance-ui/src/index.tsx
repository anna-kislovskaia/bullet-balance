import * as React from "react";
import * as ReactDOM from "react-dom";
import {AppComponent} from "./modules/app/components/app.component";

const root =  document.getElementById("root");

const render = (Component: typeof AppComponent) => {
    const mainComponent = <Component compiler="TypeScript" framework="React" />;
    ReactDOM.render(mainComponent, root);
};

render(AppComponent);

if (module.hot) {
    module.hot.accept('./modules/app/components/app.component', () => {
        console.log("Reloading Sources...");
        const nextApp = require('./modules/app/components/app.component').AppComponent; //tslint:disable-line no-require-imports
        render(nextApp);
    });
}
