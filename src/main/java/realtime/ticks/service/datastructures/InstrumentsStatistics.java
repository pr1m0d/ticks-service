package realtime.ticks.service.datastructures;

import java.util.concurrent.ConcurrentHashMap;

public class InstrumentsStatistics {

    private final ConcurrentHashMap<String, TickStatistics> instrumentsStatsMap;

    public InstrumentsStatistics() {
        this.instrumentsStatsMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, TickStatistics> getInstrumentsStatsMap() {
        return instrumentsStatsMap;
    }

    public void putTick(String key, TickStatistics tickStatistics) {
        this.instrumentsStatsMap.put(key, tickStatistics);
    }

}
