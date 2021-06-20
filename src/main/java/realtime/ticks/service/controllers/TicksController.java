package realtime.ticks.service.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.joda.time.DateTime;
import realtime.ticks.service.services.TickService;
import realtime.ticks.service.objects.Tick;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller("/ticks")
public class TicksController {

    @Inject
    TickService tickService;

    @Post("/")
    public HttpStatus validateAndStoreTick(@Body @Valid Tick tick) {
        // let's early return if the tick is too old. I'm excluding the possibility of ticks too much in the future too,
        // while a couple of seconds of sync among server time may happen more make me think that the message is from Martin McFly
        if (tick.getDateTime().isBefore(DateTime.now().minusSeconds(60)) ||
                tick.getDateTime().isAfter(DateTime.now().plusSeconds(2))) {
            return HttpStatus.NO_CONTENT;
        }
        try {
            tickService.store(tick);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.CREATED;
    }
}