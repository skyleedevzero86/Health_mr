package com.sleekydz86.core.security.ratelimit;

import com.sleekydz86.core.security.jwt.ErrorCode;
import com.sleekydz86.core.security.jwt.JwtSendErrorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final JwtSendErrorService jwtSendErrorService;
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100; // 최대 요청 수
    private static final long TIME_WINDOW = 60000; // 1분

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        RateLimitInfo rateLimitInfo = requestCounts.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        long currentTime = System.currentTimeMillis();

        if (currentTime - rateLimitInfo.getWindowStart() > TIME_WINDOW) {
            rateLimitInfo.reset(currentTime);
        }

        int currentCount = rateLimitInfo.incrementAndGet();

        if (currentCount > MAX_REQUESTS) {
            jwtSendErrorService.sendErrorResponseProcess(response, ErrorCode.RATE_LIMIT_EXCEEDED, 429); // HTTP 429 Too Many Requests
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitInfo {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart;

        public RateLimitInfo() {
            this.windowStart = System.currentTimeMillis();
        }

        public void reset(long newWindowStart) {
            count.set(0);
            this.windowStart = newWindowStart;
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public long getWindowStart() {
            return windowStart;
        }
    }
}

