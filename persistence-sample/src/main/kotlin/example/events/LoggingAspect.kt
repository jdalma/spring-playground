package example.events

import example.logger

//@Aspect
//class LoggingAspect {
//
//    val log = LoggingAspect::class.logger()
//
//    @Around("execution(* example.events..*.*(..))")
//    fun doTransaction(joinPoint: ProceedingJoinPoint): Any {
//        return try {
//            log.info("[{} 시작]", joinPoint.signature)
//
//            val proceed = joinPoint.proceed()
//
//            log.info("[{} 끝] {}", joinPoint.signature, proceed.toString())
//            proceed
//        } catch (e: Exception) {
//            log.info("[{} 롤백]", joinPoint.signature)
//            throw e
//        } finally {
//            log.info("[{} 릴리즈]", joinPoint.signature)
//        }
//    }
//}