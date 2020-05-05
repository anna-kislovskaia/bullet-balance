import * as React from "react";
import * as ReactDOM from "react-dom";
import {AppContainer} from "./modules/app/containers/app.container";

const root =  document.getElementById("root");

const render = (Component: typeof AppContainer) => {
    const mainComponent = <Component compiler="TypeScript" />;
    ReactDOM.render(mainComponent, root);
};

render(AppContainer);

if (module.hot) {
    module.hot.accept('./modules/app/containers/app.container', () => {
        console.log("Reloading Sources...");
        const nextApp = require('./modules/app/containers/app.container').AppContainer; //tslint:disable-line no-require-imports
        render(nextApp);
    });
}
