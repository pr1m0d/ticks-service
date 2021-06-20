package realtime.ticks.service.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import realtime.ticks.service.services.TickService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Controller("/statistics")
public class StatisticsController {

    @Inject
    TickService tickService;

    @Get("/")
    public Map<String, Number> globalStatistics() {
        return tickService.getGlobalStatistics().getTicksStatistics() == null ? returnEmptyMap() :
                tickService.getGlobalStatistics().getTicksStatistics().toMap();
    }

    @Get("/{instrument}")
    public Map<String, Number> getTickStatistics(@PathVariable String instrument) {
        return tickService.getInstrumentsStatistics().getInstrumentsStatsMap().get(instrument) == null ? returnEmptyMap()
                : tickService.getInstrumentsStatistics().getInstrumentsStatsMap().get(instrument).toMap();
    }

    // cold start stats requests
    private HashMap<String, Number> returnEmptyMap() {
        // to use if global and instruments are not init yet
        HashMap<String, Number> emptyMap = new HashMap<>();
        emptyMap.put("avg", 0.0);
        emptyMap.put("max", 0.0);
        emptyMap.put("min", 0.0);
        emptyMap.put("count", 0);
        return emptyMap;
    }
}