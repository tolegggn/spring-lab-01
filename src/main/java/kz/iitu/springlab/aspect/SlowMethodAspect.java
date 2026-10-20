package kz.iitu.springlab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Aspect
@Component
public class SlowMethodAspect {

    private static final Logger log = LoggerFactory.getLogger(SlowMethodAspect.class);

    @Value("${app.aspect.slow-threshold-ms:200}")
    private long thresholdMs;

    public record SlowMethodRecord(
            String methodName,
            long executionTimeMs,
            long thresholdMs,
            String timestamp,
            String arguments
    ) {}

    private final List<SlowMethodRecord> slowCalls = new CopyOnWriteArrayList<>();

    @Around("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public Object captureSlowMethod(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            if (durationMs >= thresholdMs) {
                String methodName = pjp.getSignature().toShortString();
                String args = Arrays.toString(pjp.getArgs());
                String timestamp = Instant.now().toString();
                SlowMethodRecord record = new SlowMethodRecord(
                        methodName,
                        durationMs,
                        thresholdMs,
                        timestamp,
                        args
                );
                slowCalls.add(record);
                log.warn("[SLOW-LOG-VAR2] Slow method detected: {} took {} ms (threshold: {} ms)",
                        methodName, durationMs, thresholdMs);
            }
        }
    }

    public List<SlowMethodRecord> getSlowCalls() {
        return Collections.unmodifiableList(slowCalls);
    }

    public void clearSlowCalls() {
        slowCalls.clear();
    }

    public long getThresholdMs() {
        return thresholdMs;
    }
}
