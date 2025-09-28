package example.config

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LogLevel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    @Bean
    fun defaultWebClient(): WebClient {
        val connectionProvider = ConnectionProvider.builder("my-provider")
            .maxConnections(1)
            .build();

        //  [TCP 연결] --500ms--> [요청 전송] --800ms--> [응답 수신]
        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
            .responseTimeout(Duration.ofMillis(1500))
            .wiretap("my-webclient", LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL)
            .metrics(true) { uriTagValue -> uriTagValue }
            .doOnConnected { conn ->
                conn.channel().closeFuture().addListener {
                    logger.info("[Connection Closed] : $conn")
                }
                logger.info("[New Connection] : $conn")
            }
            .doOnConnect { config ->
                logger.info("[Connection Attempt] Attempting to connect...")
            }

        return WebClient.builder()
            .baseUrl("http://localhost:9090")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    @Bean
    fun slowBodyWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .wiretap("my-wiretap", LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL)

        return WebClient.builder()
            .baseUrl("http://localhost:9090")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
