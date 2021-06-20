package realtime.ticks.service.runnables;

import realtime.ticks.service.datastructures.GlobalStatistics;
import realtime.ticks.service.datastructures.TickStatistics;
import realtime.ticks.service.datastructures.InstrumentsStatistics;
import realtime.ticks.service.objects.Tick;

import javax.inject.Inject;

public class RemoveTickFromGlobalStats implements Runnable {

    private final GlobalStatistics globalStatistics;
    private final Tick tick;

    public RemoveTickFromGlobalStats(Tick tick, GlobalStatistics globalStatistics) {
        this.tick = tick;
        this.globalStatistics = globalStatistics;
    }

    @Override
    public void run() {
        globalStatistics.getTicksStatistics().updateStats(tick, TickStatistics.Action.REMOVE);
    }
}
