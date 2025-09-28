package example.service

import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.PrematureCloseException

@Service
class PrematureCloseExceptionService(
    @Qualifier("defaultWebClient") private val defaultWebClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun case1LargeResponseTerminatedEarly(): String {
        return try {
            defaultWebClient.get()
                .uri("/internal/large-response")
                .retrieve()
                .bodyToMono(String::class.java)
                .timeout(java.time.Duration.ofMillis(500))
                .awaitSingle()
        } catch (e: Exception) {
            when (e.cause) {
                is PrematureCloseException -> {
                    logger.error("Case 1 - PrematureCloseException: Server closed connection during large response transfer", e)
                    "Case 1 - PrematureCloseException occurred: ${e.cause?.message}"
                }
                else -> {
                    logger.error("Case 1 - Other exception: ${e.message}", e)
                    "Case 1 - Exception: ${e.message}"
                }
            }
        }
    }

    suspend fun case2ConnectionIdleTimeout(delay: Int): String {
        return try {
            defaultWebClient.get()
                .uri("/internal/delay/$delay")
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    logger.error("Case 2 - PrematureCloseException: Connection closed due to idle timeout", e)
                    "Case 2 - PrematureCloseException occurred: ${e.cause?.message}"
                }
                e is WebClientResponseException -> {
                    logger.error("Case 2 - WebClient response exception", e)
                    "Case 2 - WebClient exception: ${e.message}"
                }
                else -> {
                    logger.error("Case 2 - Timeout or other exception: ${e.message}", e)
                    "Case 2 - Exception: ${e.message}"
                }
            }
        }
    }

    suspend fun case3ServerClosesEarly(): String {
        return try {
            defaultWebClient.get()
                .uri("/internal/chunked-close")
                .retrieve()
                .bodyToMono(String::class.java)
                .onErrorResume { error ->
                    when (error) {
                        is PrematureCloseException -> {
                            logger.error("Case 3 - Direct PrematureCloseException caught", error)
                            Mono.just("Case 3 - PrematureCloseException: ${error.message}")
                        }
                        else -> {
                            if (error.cause is PrematureCloseException) {
                                logger.error("Case 3 - Wrapped PrematureCloseException", error)
                                Mono.just("Case 3 - Wrapped PrematureCloseException: ${error.cause?.message}")
                            } else {
                                logger.error("Case 3 - Other error during chunked transfer", error)
                                Mono.just("Case 3 - Error: ${error.message}")
                            }
                        }
                    }
                }
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Case 3 - Unexpected exception", e)
            "Case 3 - Unexpected exception: ${e.message}"
        }
    }
}
