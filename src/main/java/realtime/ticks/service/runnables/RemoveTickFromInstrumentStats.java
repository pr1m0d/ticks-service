package realtime.ticks.service.runnables;

import realtime.ticks.service.datastructures.InstrumentsStatistics;
import realtime.ticks.service.datastructures.TickStatistics;
import realtime.ticks.service.objects.Tick;

import java.util.concurrent.ConcurrentHashMap;

public class RemoveTickFromInstrumentStats implements Runnable {

    private final InstrumentsStatistics instrumentsStatistics;
    private final Tick tick;

    public RemoveTickFromInstrumentStats(Tick tick, InstrumentsStatistics instrumentsStatistics) {
        this.tick = tick;
        this.instrumentsStatistics = instrumentsStatistics;
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, TickStatistics> ticksMap = instrumentsStatistics.getInstrumentsStatsMap();
        ticksMap.get(tick.getInstrument()).updateStats(tick, TickStatistics.Action.REMOVE);
        if (ticksMap.get(tick.getInstrument()).getCount() == 0) {
            ticksMap.remove(tick.getInstrument());
        }
    }
}
