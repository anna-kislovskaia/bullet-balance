package com.bulletbalance.model.chart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ChartPlot {
    @JsonProperty("xrange") @NotNull @NonNull
    private Range xRange;
    @JsonProperty("yrange") @NotNull @NonNull
    private Range yRange;
    @JsonProperty @NotNull @NonNull
    private List<Point> points;
}
