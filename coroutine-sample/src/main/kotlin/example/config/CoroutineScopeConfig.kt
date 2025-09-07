package example.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineScopeConfig {

    @Bean
    fun coroutineDispatcher(): CoroutineDispatcher =
        Dispatchers.Default

    @Bean
    fun exceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            println("CoroutineExceptionHandler got $throwable")
        }

    @Bean
    fun coroutineScope(
        coroutineDispatcher: CoroutineDispatcher,
        exceptionHandler: CoroutineExceptionHandler,
    ) = CoroutineScope(
        SupervisorJob() +
            coroutineDispatcher +
            exceptionHandler
    )
}
