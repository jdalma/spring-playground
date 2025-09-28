package example.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.PrematureCloseException

@RestController
@RequestMapping("/test")
class PrematureCloseTestController(
    @Qualifier("defaultWebClient") private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/force-premature-close")
    fun testForcePrematureClose(): String? {
        return try {
            logger.info("Testing force premature close...")
            webClient.get()
                .uri("/internal/force-close")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    logger.error("SUCCESS: PrematureCloseException caught!", e)
                    "SUCCESS: PrematureCloseException - ${e.cause?.message}"
                }
                e.message?.contains("Premature") == true -> {
                    logger.error("SUCCESS: Premature-related exception caught!", e)
                    "SUCCESS: Premature exception - ${e.message}"
                }
                else -> {
                    logger.error("Other exception: ${e.message}", e)
                    "Other exception: ${e.message}"
                }
            }
        }
    }

    @GetMapping("/streaming-premature/{chunks}")
    fun testStreamingPremature(@PathVariable chunks: Int): String? {
        return try {
            logger.info("Testing streaming premature close with $chunks chunks...")
            webClient.get()
                .uri("/internal/streaming-close/$chunks")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    logger.error("SUCCESS: PrematureCloseException in streaming!", e)
                    "SUCCESS: Streaming PrematureCloseException - ${e.cause?.message}"
                }
                e.message?.contains("Premature") == true -> {
                    logger.error("SUCCESS: Premature-related exception in streaming!", e)
                    "SUCCESS: Streaming premature exception - ${e.message}"
                }
                else -> {
                    logger.error("Other streaming exception: ${e.message}", e)
                    "Other streaming exception: ${e.message}"
                }
            }
        }
    }

    @GetMapping("/timeout-scenario/{seconds}")
    fun testTimeoutScenario(@PathVariable seconds: Int): String? {
        return try {
            logger.info("Testing timeout scenario with $seconds seconds delay...")
            webClient.get()
                .uri("/internal/delay/$seconds")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    logger.error("SUCCESS: PrematureCloseException from timeout!", e)
                    "SUCCESS: Timeout PrematureCloseException - ${e.cause?.message}"
                }
                e.message?.contains("timeout") == true -> {
                    logger.error("Timeout exception: ${e.message}", e)
                    "Timeout exception: ${e.message}"
                }
                else -> {
                    logger.error("Other timeout exception: ${e.message}", e)
                    "Other timeout exception: ${e.message}"
                }
            }
        }
    }

    @GetMapping("/test-abort")
    fun testAbortConnection(): String {
        logger.info("Testing abort connection scenario...")

        return try {
            val response = webClient.get()
                .uri("/force/abort-connection")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()

            "Response received: $response"
        } catch (e: Exception) {
            throw e
        }
    }

    @GetMapping("/test-chunked-abort")
    fun testChunkedAbort(): String {
        logger.info("Testing chunked abort scenario...")

        return try {
            val response = webClient.get()
                .uri("/force/chunked-abort")
                .retrieve()
                .bodyToMono(String::class.java)
                .block()

            "Response received: $response"
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    logger.error("SUCCESS: Chunked abort caused PrematureCloseException!", e)
                    "SUCCESS: Chunked abort PrematureCloseException - ${e.cause?.message}"
                }
                e is PrematureCloseException -> {
                    logger.error("SUCCESS: Direct PrematureCloseException in chunked!", e)
                    "SUCCESS: Direct chunked PrematureCloseException - ${e.message}"
                }
                else -> {
                    logger.error("Chunked abort exception: ${e.javaClass.simpleName} - ${e.message}", e)
                    "Chunked abort exception: ${e.javaClass.simpleName} - ${e.message}"
                }
            }
        }
    }
}
