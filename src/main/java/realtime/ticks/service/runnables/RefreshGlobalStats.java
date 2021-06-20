package realtime.ticks.service.runnables;

import realtime.ticks.service.datastructures.GlobalStatistics;
import realtime.ticks.service.datastructures.TickStatistics;
import realtime.ticks.service.objects.Tick;

public class RefreshGlobalStats implements Runnable {

    GlobalStatistics globalStatistics;
    private final Tick tick;

    public RefreshGlobalStats(Tick tick, GlobalStatistics globalStatistics) {
        this.tick = tick;
        this.globalStatistics = globalStatistics;
    }

    @Override
    public void run() {
        // let's update global stats
        // let's check if there are ticks for this instrument and update the map
        if (globalStatistics.getTicksStatistics() == null) {
            globalStatistics.setTicksStatistics(new TickStatistics(tick));
        } else {
            globalStatistics.getTicksStatistics().updateStats(tick, TickStatistics.Action.ADD);
        }
    }
}
