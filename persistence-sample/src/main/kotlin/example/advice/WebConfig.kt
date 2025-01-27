package example.advice

import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig: WebMvcConfigurer {

    @Bean
    fun addSuccessFilter(): FilterRegistrationBean<Filter> {
        return FilterRegistrationBean<Filter>(SuccessFilter()).apply {
            this.addUrlPatterns("/*")
        }
    }
    //
    // @Bean
    // fun addExceptionFilter(): FilterRegistrationBean<Filter> {
    //     return FilterRegistrationBean<Filter>(ExceptionFilter()).apply {
    //         this.addUrlPatterns("/advice")
    //     }
    // }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.apply {
            this.addInterceptor(SuccessInterceptor()).addPathPatterns("/*")
            // this.addInterceptor(ExceptionInterceptor()).addPathPatterns("/adviceTest")
        }
    }
}
