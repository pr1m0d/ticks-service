package realtime.ticks.service.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskScheduler
import io.micronaut.test.annotation.MicronautTest
import org.joda.time.DateTime
import realtime.ticks.service.datastructures.GlobalStatistics
import realtime.ticks.service.datastructures.InstrumentsStatistics
import realtime.ticks.service.services.TickService
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import groovyx.gpars.GParsPool

import javax.inject.Inject

@MicronautTest
class StatisticsControllerIntegrationTest extends Specification {

    @Inject
    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer

    @Inject
    TickService tickService

    @AutoCleanup
    @Shared
    @Inject
    @Client("/ticks")
    HttpClient ticksClient

    @AutoCleanup
    @Shared
    @Inject
    @Client("/statistics")
    HttpClient statisticsClient

    @Inject
    TaskScheduler taskScheduler

    def setup() {
        tickService.globalStatistics = new GlobalStatistics()
        tickService.instrumentsStatistics = new InstrumentsStatistics()
    }

    void "statistics controller fetches correctly global stats"() {
        given:
        HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test", price: "1234", timestamp: (DateTime.now().getMillis() / 1000)])
        and:
        3.times {
            ticksClient.toBlocking().exchange(postRequest)
        }
        and:
        HttpRequest postRequestMin = HttpRequest.POST("/", [instrument: "test", price: "1", timestamp: (DateTime.now().getMillis() / 1000)])
        ticksClient.toBlocking().exchange(postRequestMin)

        when:
        def response = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        then:
        response.body().get("avg") == (1234 * 3 + 1) / 4
        response.body().get("max") == 1234
        response.body().get("min") == 1
        response.body().get("count") == 4
    }

    void "statistics controller fetches correctly single instrument stats and global stats"() {
        given:
        HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test1", price: "1234", timestamp: (DateTime.now().getMillis() / 1000)])
        and:
        3.times {
            ticksClient.toBlocking().exchange(postRequest)
        }
        and:
        HttpRequest postRequestMin = HttpRequest.POST("/", [instrument: "test2", price: "1", timestamp: (DateTime.now().getMillis() / 1000)])
        2.times {
            ticksClient.toBlocking().exchange(postRequestMin)
        }
        when:
        def globalResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        def response1 = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test1"), Map)
        def response2 = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test2"), Map)
        then:
        globalResponse.body().get("avg") == ((1234 * 3) + 2) / 5
        globalResponse.body().get("max") == 1234
        globalResponse.body().get("min") == 1
        globalResponse.body().get("count") == 5
        response1.body().get("avg") == (1234 * 3) / 3
        response1.body().get("max") == 1234
        response1.body().get("min") == 1234
        response1.body().get("count") == 3
        response2.body().get("avg") == 1
        response2.body().get("max") == 1
        response2.body().get("min") == 1
        response2.body().get("count") == 2
    }

    void "ticks self-destroy correctly"() {
        given:
        3.times {
            HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test1", price: "1", timestamp: (DateTime.now().minusSeconds(55 - (it * 2)).getMillis() / 1000)])
            ticksClient.toBlocking().exchange(postRequest)
        }
        when:
        sleep(5000)
        def globalResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        def instrumentResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test1"), Map)
        then:
        globalResponse.body().get("avg") == 1.0
        globalResponse.body().get("max") == 1.0
        globalResponse.body().get("min") == 1.0
        globalResponse.body().get("count") == 2
        instrumentResponse.body().get("avg") == 1.0
        instrumentResponse.body().get("max") == 1.0
        instrumentResponse.body().get("min") == 1.0
        instrumentResponse.body().get("count") == 2

        when:
        sleep(2000)
        globalResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        instrumentResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test1"), Map)
        then:
        globalResponse.body().get("avg") == 1.0
        globalResponse.body().get("max") == 1.0
        globalResponse.body().get("min") == 1.0
        globalResponse.body().get("count") == 1
        instrumentResponse.body().get("avg") == 1.0
        instrumentResponse.body().get("max") == 1.0
        instrumentResponse.body().get("min") == 1.0
        instrumentResponse.body().get("count") == 1

        when:
        sleep(2000)
        globalResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        instrumentResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test1"), Map)
        then:
        globalResponse.body().get("avg") == 0.0
        globalResponse.body().get("max") == 0.0
        globalResponse.body().get("min") == 0.0
        globalResponse.body().get("count") == 0
        instrumentResponse.body().get("avg") == 0.0
        instrumentResponse.body().get("max") == 0.0
        instrumentResponse.body().get("min") == 0.0
        instrumentResponse.body().get("count") == 0
    }

    void "Test tick concurrency"() {
        given:
        HttpRequest postRequest = HttpRequest.POST("/", [instrument: "test1", price: "1", timestamp: (DateTime.now().getMillis() / 1000)])
        when:
        GParsPool.withPool(100) {
            (1..1000).eachParallel {
                ticksClient.toBlocking().exchange(postRequest)
            }
        }
        sleep(100)
        def globalResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET(""), Map)
        def instrumentResponse = statisticsClient.toBlocking().exchange(HttpRequest.GET("/test1"), Map)
        then:
        globalResponse.body().get("count") == 1000
        globalResponse.body().get("avg") == 1.0
        globalResponse.body().get("max") == 1.0
        globalResponse.body().get("min") == 1.0
        instrumentResponse.body().get("count") == 1000
        instrumentResponse.body().get("avg") == 1.0
        instrumentResponse.body().get("max") == 1.0
        instrumentResponse.body().get("min") == 1.0
    }

}
