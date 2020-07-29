import { BehaviorSubject, Observable } from "rxjs";

export namespace PortfolioAllocationService {
    const investments = new BehaviorSubject<number>(100000);

    export const getInvestments = (): Observable<number> => investments.asObservable();
    
    export const setInvestments = (updated: number) => investments.next(updated);
}