export declare type mapper<A, B> = (a: A) => B;

export interface Optional<T> {
    map<P>(func: mapper<T, P>): Optional<P>;
    getNullable(): T | null;
    getOrElse(other: T): T;
    chain<P>(func: mapper<T, Optional<P>>) : Optional<P>;
}

export class Some<T> implements Optional<T> {
    readonly value: T;
    constructor(value: T) {
        this.value = value;
    }

    map<P>(func: mapper<T, P>): Optional<P> {
        return new Some(func(this.value));
    }
    getNullable(): T | null {
        return this.value;
    }
    getOrElse(other: T): T {
        return this.value;
    }
    chain<P>(func: mapper<T, Optional<P>>) : Optional<P> {
        return func(this.value);
    }
}

export class None<T> implements Optional<T> {
    map<P>(func: mapper<T, P>): Optional<P> {
        return this as any;
    }
    getNullable(): T | null {
        return null;
    }
    getOrElse(other: T): T {
        return other;
    }
    chain<P>(func: mapper<T, Optional<P>>) : Optional<P> {
        return this as any;
    }
}

export const none = new None<any>();
export const some = <T>(value: T) => new Some(value);
export const fromNullable = <T>(value: T | null | undefined): Optional<T> => value ? new Some(value) : none;