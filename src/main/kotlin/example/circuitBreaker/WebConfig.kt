package example.circuitBreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class WebConfig {

    @Bean
    fun circuitBreakerConfig() = CircuitBreakerConfig.custom()
        .minimumNumberOfCalls(5)
        .slowCallRateThreshold(100f)
        .slowCallDurationThreshold(Duration.ofMillis(60000))
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .waitDurationInOpenState(Duration.ofMillis(60000))
        .build()

    @Bean
    fun translateCircuitBreaker() = CircuitBreaker.of("translate", circuitBreakerConfig())

}
