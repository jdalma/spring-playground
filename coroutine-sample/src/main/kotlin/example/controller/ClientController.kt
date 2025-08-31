package example.controller

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

@RestController
class ClientController(
    private val webClient: WebClient,
    private val restTemplate: RestTemplate
) {

    @GetMapping("/client/test")
    fun callTest(): String {
        println("[${System.currentTimeMillis()}] Starting callTest method")
        
        return runBlocking {
            // WebClient 호출을 비동기로 실행
            val webClientCall = async {
                getWebClientCall()
            }
            
            // 다른 작업을 동시에 실행
            val otherWork = async {
                println("[${System.currentTimeMillis()}] Starting other work")
                repeat(3) { i ->
                    delay(1000)
                    println("[${System.currentTimeMillis()}] Other work progress: ${i + 1}/3")
                }
                println("[${System.currentTimeMillis()}] Other work completed")
                "other work done"
            }
            
            // 추가 작업
            val additionalWork = async {
                println("[${System.currentTimeMillis()}] Starting additional work")
                delay(2000)
                println("[${System.currentTimeMillis()}] Additional work completed")
                "additional work done"
            }

            val webResult = webClientCall.await()
            val otherResult = otherWork.await()
            val additionalResult = additionalWork.await()
            
            println("[${System.currentTimeMillis()}] All works completed - WebClient: $webResult, Other: $otherResult, Additional: $additionalResult")
            webResult
        }
    }

    private suspend fun getWebClientCall(): String {
        println("[${System.currentTimeMillis()}] Starting WebClient call to /test")
        val result = webClient.get()
            .uri("/test")
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
        println("[${System.currentTimeMillis()}] WebClient call completed with result: $result")
        return result
    }
    
    @GetMapping("/client/resttemplate-test")
    fun callTestWithRestTemplate(): String {
        println("[${System.currentTimeMillis()}] Starting callTestWithRestTemplate method")
        
        return runBlocking {
            // RestTemplate 호출을 비동기로 실행 (하지만 내부적으로는 blocking)
            val restTemplateCall = async {
                getRestTemplateCall()
            }
            
            // 다른 작업을 동시에 실행
            val otherWork = async {
                println("[${System.currentTimeMillis()}] Starting other work")
                repeat(3) { i ->
                    delay(1000)
                    println("[${System.currentTimeMillis()}] Other work progress: ${i + 1}/3")
                }
                println("[${System.currentTimeMillis()}] Other work completed")
                "other work done"
            }
            
            // 추가 작업
            val additionalWork = async {
                println("[${System.currentTimeMillis()}] Starting additional work")
                delay(2000)
                println("[${System.currentTimeMillis()}] Additional work completed")
                "additional work done"
            }

            val restResult = restTemplateCall.await()
            val otherResult = otherWork.await()
            val additionalResult = additionalWork.await()
            
            println("[${System.currentTimeMillis()}] All works completed - RestTemplate: $restResult, Other: $otherResult, Additional: $additionalResult")
            restResult
        }
    }
    
    private fun getRestTemplateCall(): String {
        println("[${System.currentTimeMillis()}] Starting RestTemplate call to /test")
        val result = restTemplate.getForObject("http://localhost:8082/test", String::class.java) ?: "error"
        println("[${System.currentTimeMillis()}] RestTemplate call completed with result: $result")
        return result
    }
}
