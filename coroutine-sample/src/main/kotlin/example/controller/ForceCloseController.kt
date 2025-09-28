package example.controller

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/force")
class ForceCloseController {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    @GetMapping("/abort-connection")
    fun abortConnection(response: HttpServletResponse) {
        logger.info("Starting abort connection...")
        
        try {
            val writer = response.writer
            
            // Start writing response
            writer.write("Starting response...")
            writer.flush()

            Thread.sleep(100)

            // 일방적으로 종료 FIN 신호 전달
            response.outputStream.close()

            writer.write("This should not be sent")
            
        } catch (e: IOException) {
            logger.error("IOException during abort: ${e.message}")
        }
    }
    
    @GetMapping("/half-close/{delay}")
    suspend fun halfClose(@PathVariable delay: Long, response: HttpServletResponse): String {
        logger.info("Starting half-close response with ${delay}ms delay...")
        
        response.status = HttpServletResponse.SC_OK
        response.contentType = "text/plain"
        val writer = response.writer
        
        writer.write("Initial data sent\n")
        writer.flush()

        // Simulate processing delay
        delay(delay)
        
        // Try to write more data after delay - connection might be half-closed by client
        writer.write("Delayed data after ${delay}ms\n")
        writer.flush()
        
        return "Complete"
    }
    
    @GetMapping("/tcp-reset")
    fun tcpReset(response: HttpServletResponse) {
        logger.info("Triggering TCP RST...")
        
        try {
            response.status = HttpServletResponse.SC_OK
            val outputStream = response.outputStream
            
            // Write some initial data
            outputStream.write("Starting...".toByteArray())
            outputStream.flush()
            
            // Force RST by setting SO_LINGER with 0 timeout
            // Note: This requires access to the underlying socket which may not be directly available
            // Alternative: abruptly terminate the thread handling the connection
            
            Thread {
                Thread.sleep(50)
                // Forcefully interrupt the current thread
                Thread.currentThread().interrupt()
            }.start()
            
            Thread.sleep(100)
            outputStream.write("More data...".toByteArray())
            
        } catch (e: Exception) {
            logger.error("Exception during TCP reset: ${e.message}")
            throw RuntimeException("Connection forcefully reset")
        }
    }
    
    @GetMapping("/chunked-abort")
    fun chunkedAbort(response: HttpServletResponse) {
        logger.info("Starting chunked transfer abort...")
        
        response.status = HttpServletResponse.SC_OK
        response.setHeader("Transfer-Encoding", "chunked")
        response.contentType = "text/plain"
        
        val outputStream = response.outputStream
        
        try {
            // Send first chunk
            val chunk1 = "First chunk of data\n"
            outputStream.write(chunk1.toByteArray())
            outputStream.flush()
            
            Thread.sleep(100)
            
            // Send partial chunk then abort
            val chunk2 = "Second chunk "
            outputStream.write(chunk2.toByteArray())
            // Don't flush, just close abruptly
            
            // This simulates a connection drop mid-chunk
            outputStream.close()
            
        } catch (e: IOException) {
            logger.error("IOException during chunked abort: ${e.message}")
        }
    }
}
