import { some, Optional, none, mapper } from "../model/optional.model";


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

    optional(): Optional<T> {
        return some(this.value);
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

    optional(): Optional<T> {
        return none;
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

    optional(): Optional<T> {
        return none;
    }
}

export type Task<T> = TaskSuccess<T> | TaskFailure<T> | TaskPending<T>;

export namespace TaskUtils {
    export const pending: TaskPending<never> = new TaskPending<never>();
    export const success = <T>(result: T) => new TaskSuccess<T>(result);
    export const failure = <T>(error: Error) => new TaskFailure<T>(error);
}
