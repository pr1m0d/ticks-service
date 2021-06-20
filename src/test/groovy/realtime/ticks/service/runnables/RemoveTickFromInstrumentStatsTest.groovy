package realtime.ticks.service.runnables

import org.joda.time.DateTime
import realtime.ticks.service.datastructures.InstrumentsStatistics
import realtime.ticks.service.objects.Tick
import spock.lang.Specification

class RemoveTickFromInstrumentStatsTest extends Specification {

    InstrumentsStatistics instrumentsStatistics = new InstrumentsStatistics()
    Tick tickInstrumentOne
    Tick tickInstrumentTwo

    def setup() {
        tickInstrumentOne = new Tick(price: 1.0, instrument: "I1", timestamp: DateTime.now().millis)
        tickInstrumentTwo = new Tick(price: 1.0, instrument: "I2", timestamp: DateTime.now().millis)
        new RefreshInstrumentStats(tickInstrumentOne, instrumentsStatistics).run()
        new RefreshInstrumentStats(tickInstrumentTwo, instrumentsStatistics).run()
    }

    def "RemoveTickFromInstrumentStats removes the right tick"() {
        given:
        Runnable runnable = new RemoveTickFromInstrumentStats(tickInstrumentTwo, instrumentsStatistics)
        when:
        runnable.run()
        then:
        instrumentsStatistics.instrumentsStatsMap.size() == 1
        instrumentsStatistics.instrumentsStatsMap.get("I1")
        !instrumentsStatistics.instrumentsStatsMap.get("I2")
    }
}
