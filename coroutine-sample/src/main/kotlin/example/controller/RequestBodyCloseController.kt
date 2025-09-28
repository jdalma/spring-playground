package example.controller

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RestController
@RequestMapping("/body-close")
class RequestBodyCloseController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/close-during-read")
    suspend fun closeConnectionDuringBodyRead(
        @RequestBody(required = false) body: String?,
        request: HttpServletRequest
    ): String {
        logger.info("Received request, starting to read body...")
        
        try {
            delay(100)
            
            request.inputStream.use { stream ->
                val buffer = ByteArray(1024)
                var bytesRead = 0
                var totalBytes = 0
                
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytes += bytesRead
                    logger.info("Read $bytesRead bytes, total: $totalBytes")
                    
                    if (totalBytes > 5000) {
                        logger.info("Forcefully closing connection after reading $totalBytes bytes")
                        throw IOException("Connection closed intentionally")
                    }
                    
                    delay(50)
                }
            }
        } catch (e: Exception) {
            logger.error("Error during body read: ${e.message}")
            throw e
        }
        
        return "Should not reach here"
    }

    @PostMapping("/close-immediately")
    @ResponseStatus(HttpStatus.OK)
    fun closeImmediately(
        request: HttpServletRequest
    ) {
        logger.info("Closing connection immediately without reading body")
        
        try {
            val socket = request.getAttribute("org.apache.tomcat.util.net.SocketWrapperBase")
            if (socket != null) {
                val closeMethod = socket.javaClass.getMethod("close")
                closeMethod.invoke(socket)
                logger.info("Socket closed forcefully")
            } else {
                request.inputStream.close()
                logger.info("Input stream closed")
            }
        } catch (e: Exception) {
            logger.error("Error closing connection: ${e.message}", e)
            throw RuntimeException("Connection terminated")
        }
    }

    @PostMapping("/partial-read")
    fun partialRead(
        request: HttpServletRequest
    ): String {
        logger.info("Starting partial read of request body")
        
        try {
            val buffer = ByteArray(100)
            val bytesRead = request.inputStream.read(buffer)
            logger.info("Read only $bytesRead bytes, now closing...")
            
            request.inputStream.close()
            
            throw RuntimeException("Connection closed after partial read")
        } catch (e: Exception) {
            logger.error("Partial read error: ${e.message}")
            throw e
        }
    }
}