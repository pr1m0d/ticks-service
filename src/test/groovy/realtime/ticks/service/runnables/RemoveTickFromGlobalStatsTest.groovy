package realtime.ticks.service.runnables

import org.joda.time.DateTime
import realtime.ticks.service.datastructures.GlobalStatistics
import realtime.ticks.service.objects.Tick
import spock.lang.Specification

class RemoveTickFromGlobalStatsTest extends Specification {

    GlobalStatistics globalStatistics = new GlobalStatistics()
    Tick tickOne
    Tick tickTwo

    def setup() {
        tickOne = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        tickTwo = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        new RefreshGlobalStats(tickOne, globalStatistics).run()
        new RefreshGlobalStats(tickTwo, globalStatistics).run()
    }

    def "run remove the right tick from global statistics"() {
        given:
        Runnable runnable = new RemoveTickFromGlobalStats(tickOne, globalStatistics)
        when:
        runnable.run()
        then:
        globalStatistics.ticksStatistics.count == 1
        globalStatistics.ticksStatistics.getTicksList().find { it == tickTwo }
    }

}
