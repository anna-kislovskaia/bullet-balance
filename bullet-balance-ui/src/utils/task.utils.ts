import { of } from 'rxjs';
import { Observable } from "rxjs";
import * as qs from 'querystring';
import {AjaxRequest} from "rxjs/internal/observable/dom/AjaxObservable";
import {ajax} from "rxjs/internal/observable/dom/ajax";
import {Task, TaskUtils} from "./task.model";
import {catchError, map, startWith} from "rxjs/internal/operators";
import success = TaskUtils.success;
import failure = TaskUtils.failure;
import pending = TaskUtils.pending;

export class RemoteTaskApi {
    readonly baseHref: string;

    constructor(base?: string) {
        if (base) {
            this.baseHref = base;
        } else {
            this.baseHref = '/';
        }
    }

    private request<Response = never>(request: AjaxRequest): Observable<Task<Response>> {
        const url = `${this.baseHref}${request.url}`;

        const xhr: AjaxRequest = {
            withCredentials: true,
            responseType: 'json',
            ...request,
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
                ...request.headers,
            },
            url,
        };

        return ajax(xhr)
            .pipe(map(response => success<Response>(response.response)))
            .pipe(catchError(response => {
                return of(failure<Response>(response));
            }))
            .pipe(startWith(pending));
    };

    get<Response = never>(url: string, query?: {}): Observable<Task<Response>> {
        return this.request({
            url: this.buildUrl(url, query),
            method: 'GET',
        });
    };

    post<Response = never>(url: string, body: {}): Observable<Task<Response>> {
        return this.request({
            url,
            method: 'POST',
            body,
        });
    };

    delete<Response = never>(url: string, query = {}): Observable<Task<Response>> {
        return this.request({
            url: this.buildUrl(url, query),
            method: 'DELETE',
        });
    };

    put<Response = never>(url: string, body?: {}): Observable<Task<Response>> {
        return this.request({
            url,
            method: 'PUT',
            body,
        });
    };

    private buildUrl(url: string, query?: {}): string {
        return query ? `${url}?${qs.stringify(query)}` : url;
    }
}

export const REMOTE_API: RemoteTaskApi = new RemoteTaskApi("/");