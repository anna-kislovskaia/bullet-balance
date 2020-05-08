import { TangentPortfolioComponentProps, TangentPortfolioComponent } from "./tangent-portfolio.component";
import { TaskUtils } from "../../utils/task.model";
import { rxComponentFactory, RxProperties } from "../../utils/rx.utils";
import { MoexDemoService } from "../../services/moex-demo.service";
import { shareReplay, map } from "rxjs/internal/operators";

const defaults: Partial<TangentPortfolioComponentProps> = {
    instruments: TaskUtils.pending,
};

const props$: RxProperties<never, TangentPortfolioComponentProps> = () => {
    const instruments$ = MoexDemoService.getInstruments()
        .pipe(shareReplay(1));
    return instruments$.pipe(map(instruments => {
        return {instruments};
    }));    
}

export const TangentPortfolioContainer = rxComponentFactory<never, TangentPortfolioComponentProps>(props$, defaults)(TangentPortfolioComponent);
