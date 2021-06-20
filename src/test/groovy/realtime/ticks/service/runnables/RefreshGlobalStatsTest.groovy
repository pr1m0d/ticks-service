package realtime.ticks.service.runnables

import io.micronaut.test.annotation.MicronautTest
import org.joda.time.DateTime
import realtime.ticks.service.datastructures.GlobalStatistics
import realtime.ticks.service.objects.Tick
import spock.lang.Specification

class RefreshGlobalStatsTest extends Specification {

    GlobalStatistics globalStatistics = new GlobalStatistics();

    def "run init global history in cold start"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        Runnable runnable = new RefreshGlobalStats(tick, globalStatistics)
        when:
        runnable.run()
        then:
        globalStatistics.ticksStatistics.count == 1
        globalStatistics.ticksStatistics.getTicksList().find { it == tick }
    }

    def "run init global history"() {
        given: "a cold start tick"
        Tick tickOne = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        Runnable runnableOne = new RefreshGlobalStats(tickOne, globalStatistics)
        runnableOne.run()
        and: "a second tick"
        Tick tickTwo = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        Runnable runnableTwo = new RefreshGlobalStats(tickTwo, globalStatistics)
        when:
        runnableTwo.run()
        then:
        globalStatistics.ticksStatistics.count == 2
        globalStatistics.ticksStatistics.getTicksList().find { it == tickOne }
        globalStatistics.ticksStatistics.getTicksList().find { it == tickTwo }
    }
}
