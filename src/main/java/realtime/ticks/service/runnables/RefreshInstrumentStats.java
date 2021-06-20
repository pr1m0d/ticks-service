package realtime.ticks.service.runnables;

import realtime.ticks.service.datastructures.InstrumentsStatistics;
import realtime.ticks.service.datastructures.TickStatistics;
import realtime.ticks.service.objects.Tick;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

public class RefreshInstrumentStats implements Runnable {

    @Inject
    InstrumentsStatistics instrumentsStatistics;

    private final Tick tick;

    public RefreshInstrumentStats(Tick tick, InstrumentsStatistics instrumentsStatistics) {
        this.tick = tick;
        this.instrumentsStatistics = instrumentsStatistics;
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, TickStatistics> ticksMap = instrumentsStatistics.getInstrumentsStatsMap();
        if (ticksMap.containsKey(tick.getInstrument())) {
            ticksMap.get(tick.getInstrument()).updateStats(tick, TickStatistics.Action.ADD);
        } else {
            // if there are no ticks for this instrument we create a new entry in the map
            TickStatistics tickStatistics = new TickStatistics(tick);
            instrumentsStatistics.putTick(tick.getInstrument(), tickStatistics);
        }
    }
}
