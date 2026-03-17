package org.springframework.samples.petclinic.util

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.jmx.export.annotation.ManagedAttribute
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.util.StopWatch

@ManagedResource("petclinic:type=CallMonitor")
@Aspect
class CallMonitoringAspect {
    var enabled: Boolean = true
        @ManagedAttribute get
        @ManagedAttribute set

    private var callCount: Int = 0
    private var accumulatedCallTime: Long = 0

    @ManagedOperation
    fun reset() {
        callCount = 0
        accumulatedCallTime = 0
    }

    @ManagedAttribute
    fun getCallCount(): Int = callCount

    @ManagedAttribute
    fun getCallTime(): Long = if (callCount > 0) accumulatedCallTime / callCount else 0

    @Throws(Throwable::class)
    @Around("within(@org.springframework.stereotype.Repository *)")
    fun invoke(joinPoint: ProceedingJoinPoint): Any? {
        return if (enabled) {
            val sw = StopWatch(joinPoint.toShortString())
            sw.start("invoke")
            try {
                joinPoint.proceed()
            } finally {
                sw.stop()
                synchronized(this) {
                    callCount++
                    accumulatedCallTime += sw.totalTimeMillis
                }
            }
        } else {
            joinPoint.proceed()
        }
    }
}
