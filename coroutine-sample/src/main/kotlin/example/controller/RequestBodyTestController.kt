package example.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.netty.http.client.PrematureCloseException

@RestController
@RequestMapping("/test-body")
class RequestBodyTestController(
    @Qualifier("defaultWebClient") private val webClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/send-large-body/{size}")
    fun testLargeBodyClose(@PathVariable size: Int): String {
        logger.info("Testing with large body of size: $size KB")
        
        val largeBody = "X".repeat(size * 1024)
        
        return try {
            val response = webClient.post()
                .uri("http://localhost:8080/body-close/close-during-read")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(largeBody), String::class.java)
                .retrieve()
                .bodyToMono<String>()
                .block()
            
            "Unexpected success: $response"
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    val message = e.cause?.message ?: ""
                    logger.error("SUCCESS: PrematureCloseException caught - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: PrematureCloseException - $message"
                    }
                }
                e is PrematureCloseException -> {
                    val message = e.message ?: ""
                    logger.error("SUCCESS: Direct PrematureCloseException - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: Direct PrematureCloseException - $message"
                    }
                }
                else -> {
                    logger.error("Other exception: ${e.javaClass.simpleName} - ${e.message}", e)
                    "Other exception: ${e.javaClass.simpleName} - ${e.message}"
                }
            }
        }
    }

    @GetMapping("/send-streaming-body")
    fun testStreamingBodyClose(): String {
        logger.info("Testing with streaming body")
        
        val streamingBody = Mono.just("Start")
            .concatWith(Mono.just("-".repeat(10000)).repeat(100))
            .reduce { acc, curr -> acc + curr }
        
        return try {
            val response = webClient.post()
                .uri("http://localhost:8080/body-close/close-immediately")
                .contentType(MediaType.TEXT_PLAIN)
                .body(streamingBody, String::class.java)
                .retrieve()
                .bodyToMono<String>()
                .block()
            
            "Unexpected success: $response"
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    val message = e.cause?.message ?: ""
                    logger.error("SUCCESS: Streaming PrematureCloseException - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception during streaming - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: Streaming PrematureCloseException - $message"
                    }
                }
                e is PrematureCloseException -> {
                    val message = e.message ?: ""
                    logger.error("SUCCESS: Direct streaming PrematureCloseException - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception during streaming - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: Direct streaming PrematureCloseException - $message"
                    }
                }
                else -> {
                    logger.error("Streaming exception: ${e.javaClass.simpleName} - ${e.message}", e)
                    "Streaming exception: ${e.javaClass.simpleName} - ${e.message}"
                }
            }
        }
    }

    @GetMapping("/send-chunked-body")
    fun testChunkedBodyClose(): String {
        logger.info("Testing with chunked body transfer")
        
        return try {
            val response = webClient.post()
                .uri("http://localhost:8080/body-close/partial-read")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just("{\"data\":\"" + "X".repeat(50000) + "\"}"), String::class.java)
                .retrieve()
                .bodyToMono<String>()
                .block()
            
            "Unexpected success: $response"
        } catch (e: Exception) {
            when {
                e.cause is PrematureCloseException -> {
                    val message = e.cause?.message ?: ""
                    logger.error("SUCCESS: Chunked PrematureCloseException - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception with chunked - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: Chunked PrematureCloseException - $message"
                    }
                }
                e is PrematureCloseException -> {
                    val message = e.message ?: ""
                    logger.error("SUCCESS: Direct chunked PrematureCloseException - $message", e)
                    when {
                        message.contains("BEFORE response, while sending request body") -> 
                            "✅ SUCCESS: Got expected exception with chunked - Connection closed BEFORE response while sending request body"
                        else -> 
                            "SUCCESS: Direct chunked PrematureCloseException - $message"
                    }
                }
                else -> {
                    logger.error("Chunked exception: ${e.javaClass.simpleName} - ${e.message}", e)
                    "Chunked exception: ${e.javaClass.simpleName} - ${e.message}"
                }
            }
        }
    }
}