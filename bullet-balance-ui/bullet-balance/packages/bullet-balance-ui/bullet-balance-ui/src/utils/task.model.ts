
export declare type mapper<A, B> = (a: A) => B;

export class TaskSuccess<T>  {
    readonly value: T;
    constructor(value: T) {
        this.value = value;
    }
    isSuccess(){
        return true;
    };
    isFailure(){
        return false;
    }
    isPending(){
        return false;
    }
    map<P>(func: mapper<T, P>): Task<P> {
        return new TaskSuccess(func(this.value));
    }
}

export class TaskFailure<T> {
    readonly error: Error;
    constructor(err: Error) {
        this.error = err;
    }

    isSuccess(){
        return false;
    };
    isFailure(){
        return true;
    }
    isPending(){
        return false;
    }
    map<P>(func: mapper<T, P>): Task<P> {
        return this as any;
    }
}

export class TaskPending<T> {
    isSuccess(){
        return false;
    };
    isFailure(){
        return false;
    }
    isPending(){
        return true;
    }
    map<P>(func: mapper<T, P>): Task<P> {
        return this as any;
    }
}

export type Task<T> = TaskSuccess<T> | TaskFailure<T> | TaskPending<T>;

export namespace TaskUtils {
    export const pending: TaskPending<never> = new TaskPending<never>();
    export const success = <T>(result: T) => new TaskSuccess<T>(result);
    export const failure = <T>(error: Error) => new TaskFailure(error);
}
