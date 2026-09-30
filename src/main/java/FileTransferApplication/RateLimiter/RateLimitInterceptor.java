package FileTransferApplication.RateLimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) return true;

        String clientIp = request.getRemoteAddr();
        String endpoint = handlerMethod.getBeanType().getSimpleName()
                + "." + handlerMethod.getMethod().getName();
        String key = clientIp + "|" + endpoint;

        Bucket bucket = buckets.computeIfAbsent(key, newKey -> createBucket(rateLimit));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) return true;

        long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setContentType("text/plain");
        response.getWriter().write("Too many requests. Try again in " + waitSeconds + " seconds.");

        return false;
    }
    private Bucket createBucket(RateLimit rateLimit) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimit.maximumRequestsAllowed())
                .refillGreedy(rateLimit.maximumRequestsAllowed(), Duration.ofSeconds(rateLimit.windowLengthInSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
