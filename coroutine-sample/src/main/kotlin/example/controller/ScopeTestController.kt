package example.controller

import kotlinx.coroutines.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ScopeTestController {
    
    // 일반 Job 사용 (위험!)
    private var normalScope = CoroutineScope(Dispatchers.IO)
    
    // SupervisorJob 사용 (안전!)
    private val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @GetMapping("/scope/normal/crash")
    fun crashNormalScope(): Map<String, Any> {
        val before = mapOf(
            "isActive" to normalScope.isActive,
            "isCancelled" to normalScope.coroutineContext.job.isCancelled
        )
        
        // 예외 발생 -> 스코프 취소됨
        normalScope.launch {
            throw RuntimeException("Boom!")
        }
        
        Thread.sleep(100) // 예외 전파 대기
        
        val after = mapOf(
            "isActive" to normalScope.isActive,
            "isCancelled" to normalScope.coroutineContext.job.isCancelled
        )
        
        return mapOf(
            "before" to before,
            "after" to after,
            "message" to "Normal scope is now dead forever!"
        )
    }
    
    @GetMapping("/scope/normal/try-use")
    fun tryUseNormalScope(): Map<String, Any> {
        val isActive = normalScope.isActive
        
        // 취소된 스코프에서 launch 시도
        val job = normalScope.launch {
            println("This will never execute if scope is cancelled")
        }
        
        return mapOf(
            "scopeActive" to isActive,
            "jobActive" to job.isActive,
            "jobCancelled" to job.isCancelled,
            "message" to if (!isActive) "Scope is dead! Launch was ignored!" else "Scope is alive"
        )
    }
    
    @GetMapping("/scope/normal/recreate")
    fun recreateNormalScope(): Map<String, Any> {
        val oldScope = normalScope
        val wasActive = oldScope.isActive
        
        // 새로운 스코프로 교체 (유일한 방법!)
        normalScope = CoroutineScope(Dispatchers.IO)
        
        return mapOf(
            "oldScopeActive" to wasActive,
            "newScopeActive" to normalScope.isActive,
            "message" to "Created new scope (old one cannot be revived)"
        )
    }
    
    @GetMapping("/scope/supervisor/crash")
    fun crashSupervisorScope(): Map<String, Any> {
        val before = supervisorScope.isActive
        
        // 예외 발생해도 스코프는 살아있음
        supervisorScope.launch {
            throw RuntimeException("Boom!")
        }
        
        Thread.sleep(100)
        
        val after = supervisorScope.isActive
        
        return mapOf(
            "beforeActive" to before,
            "afterActive" to after,
            "message" to "SupervisorScope survives child failures!"
        )
    }
    
    @GetMapping("/scope/demonstration")
    fun demonstrateCancellation(): Map<String, Any> {
        // 새 스코프 생성
        val testScope = CoroutineScope(Dispatchers.IO)
        
        val step1 = "Active: ${testScope.isActive}"
        
        // 명시적 취소
        testScope.cancel()
        
        val step2 = "After cancel - Active: ${testScope.isActive}, Cancelled: ${testScope.coroutineContext.job.isCancelled}"
        
        // 취소된 스코프에서 launch 시도
        val job = testScope.launch {
            println("Won't run")
        }
        
        val step3 = "New job - Active: ${job.isActive}, Cancelled: ${job.isCancelled}"
        
        // 재활성화 시도 (불가능!)
        // testScope.activate() // 이런 메소드는 존재하지 않음
        // testScope.restart()  // 이것도 불가능
        
        return mapOf(
            "step1_initial" to step1,
            "step2_afterCancel" to step2,
            "step3_tryLaunch" to step3,
            "conclusion" to "Once cancelled, always cancelled. Must create new scope!"
        )
    }
}