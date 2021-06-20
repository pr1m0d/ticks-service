package realtime.ticks.service.objects;

import io.micronaut.core.annotation.Introspected;
import org.joda.time.DateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
public class Tick {
    @NotNull
    private
    double price;
    @NotBlank
    private
    String instrument;
    @NotBlank
    private
    DateTime timestamp;

    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public DateTime getDateTime() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = new DateTime(timestamp * 1000L);
    }

}
