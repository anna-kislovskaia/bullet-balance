import {Component, createElement, ComponentClass, ComponentType} from 'react';
import {BehaviorSubject, Observable, Subscription} from "rxjs/index";
import {animationFrame} from "rxjs/internal/scheduler/animationFrame";
import {observeOn} from "rxjs/internal/operators";

export type ObservableProps<T> = {
    [K in keyof T]: Observable<T[K]>;
}
export type Omit<T, K> = Pick<T, Exclude<keyof T, K>>;
export type RxComponentFactory<K extends keyof P, P> = (Target: ComponentType<P>) => ComponentClass<Readonly<Pick<P, K>>>;
export type RxProperties<K extends keyof P, P> = (props$: Observable<Readonly<Partial<P>>>) => Observable<Readonly<Omit<P, K>>>;


export function rxComponentFactory<K extends keyof P, P extends object = never>(
    select: RxProperties<K, P>,
    defaults?: Partial<P>
): RxComponentFactory<K, P> {
    return Target => {
        class RxComponent extends Component<Readonly<P>> {
            static displayName = `RxComponent(${Target.displayName || Target.name})`;

            private props$ = new BehaviorSubject(this.props);
            private results$ = select(this.props$.asObservable());
            private resultsSubscription?: Subscription;

            componentWillMount() {
                this.resultsSubscription = this.results$
                    .pipe(observeOn(animationFrame))
                    .subscribe(state => this.setState(state));
            }

            componentWillReceiveProps(props: Readonly<P>) {
                this.props$.next(props);
            }

            componentWillUnmount() {
                if (this.resultsSubscription) {
                    this.resultsSubscription.unsubscribe();
                }
            }

            render() {
                const componentProperties = defaults ? Object.assign({}, defaults) : {};
                return createElement(Target, Object.assign(componentProperties, this.props, this.state));
            }
        }

        return RxComponent;
    };
}