package realtime.ticks.service.datastructures

import realtime.ticks.service.objects.Tick
import spock.lang.Specification

class TickStatisticsTest extends Specification {

    def "TickStatistic Initialization works as expected"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        expect:
        tickStatistics.ticksList.contains(tick)
        tickStatistics.avg == tick.price
        tickStatistics.max == tick.price
        tickStatistics.min == tick.price
    }

    def "updateStats works as expected when a new tick arrive"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        Tick newTick = new Tick(price: 5.0, instrument: "test", timestamp: 1891588259)
        when:
        tickStatistics.updateStats(newTick, TickStatistics.Action.ADD)
        then:
        tickStatistics.ticksList.containsAll([tick, newTick])
        tickStatistics.avg == (tick.price + newTick.price) / tickStatistics.ticksList.size()
        tickStatistics.max == Math.max(tick.price, newTick.price)
        tickStatistics.min == Math.min(tick.price, newTick.price)
    }

    def "updateStats works as expected adding multiple ticks"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        when:
        10.times {
            tickStatistics.updateStats(new Tick(price: 5.0, instrument: "test", timestamp: 1891588259), TickStatistics.Action.ADD)
        }
        then:
        tickStatistics.ticksList.size() == 11
        Double expectedAvg = ((10.0D * 5.0D) + 1.0D) / (tickStatistics.ticksList.size() as Double)
        tickStatistics.avg == expectedAvg
        tickStatistics.max == 5.0
        tickStatistics.min == 1.0
    }

    def "updateStats works as expected when if last tick gets removed"() {
        given:
        Tick tick = new Tick(price: 1.0, instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        when:
        tickStatistics.updateStats(tick, TickStatistics.Action.REMOVE)
        then:
        tickStatistics.ticksList.isEmpty()
        tickStatistics.avg == 0.0
        tickStatistics.max == 0.0
        tickStatistics.min == 0.0
    }

    def "updateStats works as expected when if a ticks gets removed"() {
        given:
        Tick tick1 = new Tick(price: 3.0, instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick1)
        Tick tick2 = new Tick(price: 5.0, instrument: "test", timestamp: 1891588259)
        Tick tick3 = new Tick(price: 7.0, instrument: "test", timestamp: 1891588259)
        when:
        tickStatistics.updateStats(tick2, TickStatistics.Action.ADD)
        tickStatistics.updateStats(tick3, TickStatistics.Action.ADD)
        tickStatistics.updateStats(tick2, TickStatistics.Action.REMOVE)
        then:
        tickStatistics.ticksList.contains(tick1)
        tickStatistics.ticksList.contains(tick3)
        !tickStatistics.ticksList.contains(tick2)
        tickStatistics.avg == 5.0
        tickStatistics.max == 7.0
        tickStatistics.min == 3.0
    }

    def "updateStats works as expected when if some ticks gets removed"() {
        given:
        Tick tick = new Tick(price: (Random.newInstance().nextInt(1000) as double), instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        ArrayList<Tick> ticksToAdd = []
        ArrayList<Tick> removedTicks = []
        999.times {
            Tick newTick = new Tick(price: (Random.newInstance().nextInt(1000) as double), instrument: "test", timestamp: 1891588259)
            ticksToAdd.add(newTick)
            tickStatistics.updateStats(newTick, TickStatistics.Action.ADD)
        }
        when:
        100.times {
            Tick toRemove = ticksToAdd.remove(0)
            removedTicks.add(toRemove)
            tickStatistics.updateStats(toRemove, TickStatistics.Action.REMOVE)
        }
        then:
        !tickStatistics.ticksList.intersect(removedTicks)
        ArrayList<Tick> expectedTicks = (ticksToAdd - removedTicks) + tick
        tickStatistics.ticksList.containsAll(expectedTicks)
        // don't wanna have the test failing for approximations coming out from the division
        BigDecimal ticksStatisticsAvg = new BigDecimal(tickStatistics.avg).setScale(3, BigDecimal.ROUND_DOWN)
        BigDecimal expectedTicksAvg = new BigDecimal(expectedTicks*.price.sum() / (expectedTicks.size() as double)).setScale(3, BigDecimal.ROUND_DOWN)
        ticksStatisticsAvg.equals(expectedTicksAvg)
        Double.compare(tickStatistics.max, expectedTicks*.price.max()) == 0
        Double.compare(tickStatistics.min, expectedTicks*.price.min()) == 0
    }

    def "ToMap works as expected"() {
        given:
        Tick tick = new Tick(price: (Random.newInstance().nextInt(1000) as double), instrument: "test", timestamp: 1891588259)
        TickStatistics tickStatistics = new TickStatistics(tick)
        when:
        HashMap expectedMap = tickStatistics.toMap()
        then:
        expectedMap.get("avg") == tick.price
        expectedMap.get("max") == tick.price
        expectedMap.get("min") == tick.price
        expectedMap.get("count") == 1
    }
}
