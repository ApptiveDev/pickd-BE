package back.pickd.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 모든 Service 클래스의 메서드 실행 로깅 Aspect.
 * <ul>
 *   <li>메서드 시작: 클래스명, 메서드명, 인자 출력</li>
 *   <li>메서드 종료: 실행 시간(ms) 출력</li>
 *   <li>예외 발생: 예외 타입과 메시지 ERROR 레벨로 출력</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(* back.pickd..*Service.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        Object[] args     = joinPoint.getArgs();

        log.info("[START] {}.{}() | args={}", className, methodName, formatArgs(args));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[END]   {}.{}() | {}ms", className, methodName, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[ERROR] {}.{}() | {}ms | {} : {}",
                    className, methodName, elapsed,
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName() + "(" + truncate(arg.toString()) + ")")
                .reduce("[", (a, b) -> a + (a.equals("[") ? "" : ", ") + b) + "]";
    }

    private String truncate(String value) {
        return value.length() > 100 ? value.substring(0, 100) + "..." : value;
    }
}
