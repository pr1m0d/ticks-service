package realtime.ticks.service.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.joda.time.DateTime
import realtime.ticks.service.objects.Tick
import realtime.ticks.service.services.TickService
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class TicksControllerTest extends Specification {

    @Inject
    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer

    @Shared
    @Inject
    @Client("/ticks")
    RxHttpClient ticksClient

    @Inject
    TicksController ticksController

    TickService tickServiceMock

    def setup() {
        tickServiceMock = Mock(TickService)
        ticksController.tickService = tickServiceMock
    }

    def "We accept ticks with the actual timestamp"() {
        given:
        HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test", price: "1234", timestamp: (DateTime.now().getMillis() / 1000)])
        when:
        HttpResponse response = ticksClient.toBlocking().exchange(postRequest)
        then:
        response.status == HttpStatus.CREATED
        1 * tickServiceMock.store(_ as Tick)
    }

    def "We reject ticks with older than 60 secs"() {
        given:
        HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test", price: "1234", timestamp: (DateTime.now().minusSeconds(61).getMillis() / 1000)])
        when:
        HttpResponse response = ticksClient.toBlocking().exchange(postRequest)
        then:
        response.status == HttpStatus.NO_CONTENT
    }

}
