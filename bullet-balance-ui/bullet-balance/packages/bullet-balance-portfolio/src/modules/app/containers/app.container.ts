import { ComponentClass, Component } from 'react';
import { RxProperties, Omit, rxComponentFactory} from "../../../utils/rx.utils";
import {AppComponent, AppComponentProps} from "../components/app.component";
import { of } from 'rxjs';

type DefaultProperties = 'compiler';

const props$: RxProperties<DefaultProperties, AppComponentProps> = () => {
    const result: Omit<AppComponentProps, DefaultProperties> = { framework: 'React'};
    return of(result);
};

export const AppContainer = rxComponentFactory<DefaultProperties, AppComponentProps>(props$)(AppComponent);
