package realtime.ticks.service.datastructures;

public class GlobalStatistics {

    private TickStatistics ticksStatistics;

    public GlobalStatistics() {
        this.ticksStatistics = null;
    }

    public TickStatistics getTicksStatistics() {
        return ticksStatistics;
    }

    public void setTicksStatistics(TickStatistics ticksStatistics) {
        this.ticksStatistics = ticksStatistics;
    }

}
