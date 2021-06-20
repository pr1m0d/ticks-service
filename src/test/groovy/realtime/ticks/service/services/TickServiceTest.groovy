package realtime.ticks.service.services

import io.micronaut.scheduling.TaskScheduler
import io.micronaut.test.annotation.MicronautTest
import org.joda.time.DateTime
import realtime.ticks.service.datastructures.GlobalStatistics
import realtime.ticks.service.datastructures.InstrumentsStatistics
import realtime.ticks.service.datastructures.TickStatistics
import realtime.ticks.service.objects.Tick
import realtime.ticks.service.runnables.RefreshGlobalStats
import realtime.ticks.service.runnables.RefreshInstrumentStats
import realtime.ticks.service.runnables.RemoveTickFromGlobalStats
import realtime.ticks.service.runnables.RemoveTickFromInstrumentStats
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration

@MicronautTest
class TickServiceTest extends Specification {

    @Inject
    TickService tickService
    @Inject
    TaskScheduler taskSchedulerMock

    def setup() {
        taskSchedulerMock = Mock(TaskScheduler)
        tickService.taskScheduler = taskSchedulerMock
    }

    def "store() schedule the 4 runnables to handle the new tick"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().plusSeconds(10).millis / 1000)
        and:
        def serviceSpy = Spy(tickService)
        when:
        serviceSpy.store(tick)
        then:
        1 * taskSchedulerMock.schedule(_ as Duration, _ as RefreshGlobalStats)
        1 * taskSchedulerMock.schedule(_ as Duration, _ as RefreshInstrumentStats)
        1 * taskSchedulerMock.schedule(_ as Duration, _ as RemoveTickFromGlobalStats)
        1 * taskSchedulerMock.schedule(_ as Duration, _ as RemoveTickFromInstrumentStats)
    }

    def "calculateExecutionDelayForGlobalStats consider the actual size of the global stats ticks array and remove a tick earlier to compensate exec time"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis / 1000)
        and:
        tickService.globalStatistics = new GlobalStatistics()
        tickService.globalStatistics.setTicksStatistics(new TickStatistics(tick))
        int ticksToAdd = numberOfTicks
        ticksToAdd.times {
            tickService.globalStatistics.ticksStatistics.updateStats(tick, TickStatistics.Action.ADD)
        }
        when:
        long result = tickService.calculateExecutionDelayForGlobalStats(tick)
        then:
        if (numberOfTicks > 10000) {
            result <= expectedResult
        } else {
            result > expectedResult
        }
        where:
        numberOfTicks | expectedResult
        0             | 59000
        100           | 59000
        1000          | 58000
        10000         | 50000
        100000        | 50000
        200000        | 40000
        500000        | 30000
        1000000       | 0
    }

    def "calculateExecutionDelayForInstrumentStats consider the actual size of the global stats ticks array and remove a tick earlier to compensate exec time"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis / 1000)
        and:
        tickService.instrumentsStatistics = new InstrumentsStatistics()
        TickStatistics tickStatistics = new TickStatistics(tick)
        tickService.instrumentsStatistics.putTick(tick.instrument, tickStatistics)
        int ticksToAdd = numberOfTicks
        ticksToAdd.times {
            tickService.instrumentsStatistics.getInstrumentsStatsMap().get(tick.instrument).updateStats(tick, TickStatistics.Action.ADD)
        }
        when:
        long result = tickService.calculateExecutionDelayForInstrumentStats(tick)
        then:
        if (numberOfTicks > 10000) {
            result <= expectedResult
        } else {
            result > expectedResult
        }
        where:
        numberOfTicks | expectedResult
        0             | 59000
        100           | 59000
        1000          | 58000
        10000         | 50000
        100000        | 50000
        200000        | 40000
        500000        | 30000
        1000000       | 0
    }
}
