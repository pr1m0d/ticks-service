package realtime.ticks.service.datastructures;

import realtime.ticks.service.objects.Tick;

import java.util.*;

public class TickStatistics {

    private double avg;
    private double max;
    private double min;
    Comparator<Tick> comparator = Comparator.comparing(Tick::getPrice);

    public ArrayList<Tick> getTicksList() {
        return ticksList;
    }

    private final ArrayList<Tick> ticksList;

    public TickStatistics(Tick tick) {
        this.ticksList = new ArrayList<>();
        this.ticksList.add(tick);
        this.avg = tick.getPrice();
        this.max = tick.getPrice();
        this.min = tick.getPrice();
    }

    public enum Action {
        ADD, REMOVE;
    }

    // one synchronized method for add and remove otherwise concurrent modification exception is waiting for us
    public synchronized void updateStats(Tick tick, Action action) {
        if (action == Action.ADD) {
            addTick(tick);
            return;
        }
        removeTick(tick);
    }

    private void addTick(Tick tick) {
        if (ticksList.isEmpty()) {
            this.avg = tick.getPrice();
            this.max = tick.getPrice();
            this.min = tick.getPrice();
            this.ticksList.add(tick);
            return;
        }
        this.avg = calculateAverage(this.avg, this.ticksList.size(), tick.getPrice(), Action.ADD);
        this.ticksList.add(tick);
        if (this.max < tick.getPrice()) {
            this.max = tick.getPrice();
        }
        if (this.min > tick.getPrice()) {
            this.min = tick.getPrice();
        }
    }

    private void removeTick(Tick tick) {
        this.avg = calculateAverage(this.avg, this.ticksList.size(), tick.getPrice(), Action.REMOVE);
        this.ticksList.remove(tick);
        if (ticksList.isEmpty()) {
            this.avg = 0.0;
            this.max = 0.0;
            this.min = 0.0;
            return;
        }
        updateMaxAndMin();
    }

    private void updateMaxAndMin() {
        if (ticksList.size() == 1) {
            this.max = ticksList.get(0).getPrice();
            this.min = ticksList.get(0).getPrice();
        }
        // let's order, O(nlogn)
        ticksList.sort(comparator);
        this.max = ticksList.get(ticksList.size() - 1).getPrice();
        this.min = ticksList.get(0).getPrice();
    }

    private double calculateAverage(double average, double size, double value, Action action) {
        if (size == 0) {
            return value;
        }
        if (action == Action.ADD) {
            return (size * average + value) / (size + 1);
        }
        return (size * average - value) / (size - 1);
    }

    public Map<String, Number> toMap() {
        Map<String, Number> stats = new HashMap<>();
        stats.put("avg", this.getAvg());
        stats.put("max", this.max);
        stats.put("min", this.min);
        stats.put("count", this.getCount());
        return stats;
    }

    public long getCount() {
        return ticksList.size();
    }

    public Double getAvg() {
        return avg;
    }

}
