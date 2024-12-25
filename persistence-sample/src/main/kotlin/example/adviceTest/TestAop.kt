package example.adviceTest

import example.logger
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.lang.Exception

class SuccessFilter : Filter {

    private val logger = SuccessFilter::class.logger()

    override fun init(filterConfig: FilterConfig?) {
        logger.info("success filter - init(), hashcode: ${System.identityHashCode(this)}")
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        logger.info("success filter - doFilter(), hashcode: ${System.identityHashCode(this)}")
        chain?.doFilter(request, response)
    }

    override fun destroy() {
        logger.info("success filter - destroy(), hashcode: ${System.identityHashCode(this)}")
    }
}

class ExceptionFilter : Filter {

    private val logger = ExceptionFilter::class.logger()

    override fun init(filterConfig: FilterConfig?) {
        logger.info("exception filter - init(), hashcode: ${System.identityHashCode(this)}")
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        logger.info("exception filter - doFilter(), hashcode: ${System.identityHashCode(this)}")
        throw FilterException("FilterException 발생!!!")
    }

    override fun destroy() {
        logger.info("exception filter - destroy(), hashcode: ${System.identityHashCode(this)}")
    }
}

class SuccessInterceptor : HandlerInterceptor {

    private val logger = SuccessInterceptor::class.logger()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        logger.info("success interceptor - preHandle(), hashcode: ${System.identityHashCode(this)}")
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        logger.info("success interceptor - postHandle(), hashcode: ${System.identityHashCode(this)}")
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        logger.info("success interceptor - afterCompletion(), hashcode: ${System.identityHashCode(this)}")
        super.afterCompletion(request, response, handler, ex)
    }
}

class ExceptionInterceptor : HandlerInterceptor {

    private val logger = ExceptionInterceptor::class.logger()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        logger.info("exception interceptor - preHandle(), hashcode: ${System.identityHashCode(this)}")
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        logger.info("exception interceptor - postHandle(), hashcode: ${System.identityHashCode(this)}")
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        logger.info("exception interceptor - afterCompletion(), hashcode: ${System.identityHashCode(this)}")
    }
}

