package realtime.ticks.service.services;

import io.micronaut.scheduling.TaskScheduler;
import org.joda.time.DateTime;
import realtime.ticks.service.datastructures.GlobalStatistics;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import realtime.ticks.service.datastructures.InstrumentsStatistics;
import realtime.ticks.service.objects.Tick;
import realtime.ticks.service.runnables.RefreshGlobalStats;
import realtime.ticks.service.runnables.RefreshInstrumentStats;
import realtime.ticks.service.runnables.RemoveTickFromGlobalStats;
import realtime.ticks.service.runnables.RemoveTickFromInstrumentStats;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
public class TickService {
    @Inject
    TaskScheduler taskScheduler;

    InstrumentsStatistics instrumentsStatistics = new InstrumentsStatistics();
    GlobalStatistics globalStatistics = new GlobalStatistics();

    public void store(Tick tick) {
        taskScheduler.schedule(Duration.ofMillis(0), new RefreshGlobalStats(tick, globalStatistics));
        taskScheduler.schedule(Duration.ofMillis(0), new RefreshInstrumentStats(tick, instrumentsStatistics));
        // let's make the tick self-destruct when it's time is over (https://www.youtube.com/watch?v=Agmu6WFdcLY)
        taskScheduler.schedule(Duration.ofMillis(calculateExecutionDelayForGlobalStats(tick)), new RemoveTickFromGlobalStats(tick, globalStatistics));
        taskScheduler.schedule(Duration.ofMillis(calculateExecutionDelayForInstrumentStats(tick)), new RemoveTickFromInstrumentStats(tick, instrumentsStatistics));
        System.out.println(globalStatistics.getTicksStatistics().getTicksList().size());
    }

    public InstrumentsStatistics getInstrumentsStatistics() {
        return instrumentsStatistics;
    }

    public GlobalStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    /**
     * Compensate execution time for the update of global stats on removal using the size of the ticks array
     */
    private long calculateExecutionDelayForGlobalStats(Tick tick) {
        Period period = new Period(DateTime.now(), tick.getDateTime().plusSeconds(60), PeriodType.millis());

        if (globalStatistics.getTicksStatistics() == null) {
            return period.getMillis();
        }
        // the 10 needs tuning but i'm just doing an hypothesis here
        long delaySizeDependent = (globalStatistics.getTicksStatistics().getTicksList().size() / 10);
        return Math.max(period.getMillis() - delaySizeDependent, 0);
    }

    /**
     * Compensate execution time for the update of instruments stats on removal using the size of the ticks array
     */
    private long calculateExecutionDelayForInstrumentStats(Tick tick) {
        Period period = new Period(DateTime.now(), tick.getDateTime().plusSeconds(60), PeriodType.millis());
        if (instrumentsStatistics.getInstrumentsStatsMap().get(tick.getInstrument()) == null) {
            return period.getMillis();
        }
        // the 10 needs tuning but i'm just doing an hypothesis here
        long delaySizeDependent = (instrumentsStatistics.getInstrumentsStatsMap().get(tick.getInstrument()).getTicksList().size() / 10);
        return Math.max(period.getMillis() - delaySizeDependent, 0);
    }
}