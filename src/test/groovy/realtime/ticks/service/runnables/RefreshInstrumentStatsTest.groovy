package realtime.ticks.service.runnables

import org.joda.time.DateTime
import realtime.ticks.service.datastructures.InstrumentsStatistics
import realtime.ticks.service.objects.Tick
import spock.lang.Specification

class RefreshInstrumentStatsTest extends Specification {

    InstrumentsStatistics instrumentsStatistics = new InstrumentsStatistics();

    def "run init global history in cold start"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: DateTime.now().millis)
        Runnable runnable = new RefreshInstrumentStats(tick, instrumentsStatistics)
        when:
        runnable.run()
        then:
        instrumentsStatistics.instrumentsStatsMap.size() == 1
        instrumentsStatistics.instrumentsStatsMap.get("test")
    }

    def "run init global history"() {
        given: "a cold start tick"
        Tick tickOne = new Tick(price: 1.0, instrument: "test1", timestamp: DateTime.now().millis)
        Runnable runnableOne = new RefreshInstrumentStats(tickOne, instrumentsStatistics)
        runnableOne.run()
        and: "a second tick"
        Tick tickTwo = new Tick(price: 1.0, instrument: "test2", timestamp: DateTime.now().millis)
        Runnable runnableTwo = new RefreshInstrumentStats(tickTwo, instrumentsStatistics)
        when:
        runnableTwo.run()
        then:
        instrumentsStatistics.instrumentsStatsMap.size() == 2
        instrumentsStatistics.instrumentsStatsMap.get("test1")
        instrumentsStatistics.instrumentsStatsMap.get("test2")
    }
}
