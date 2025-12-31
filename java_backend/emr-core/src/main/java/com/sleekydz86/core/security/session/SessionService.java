package com.sleekydz86.core.security.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionService {

    private final Map<Long, SessionInfo> sessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    public void createSession(Long userId, String ipAddress) {
        SessionInfo sessionInfo = new SessionInfo(userId, ipAddress, System.currentTimeMillis());
        sessions.put(userId, sessionInfo);
        log.debug("세션 생성: userId={}, ip={}", userId, ipAddress);
    }

    public boolean isValidSession(Long userId, String ipAddress) {
        SessionInfo sessionInfo = sessions.get(userId);
        if (sessionInfo == null) {
            return false;
        }

        if (System.currentTimeMillis() - sessionInfo.getLastAccessTime() > SESSION_TIMEOUT) {
            sessions.remove(userId);
            log.debug("세션 타임아웃: userId={}", userId);
            return false;
        }

        if (!sessionInfo.getIpAddress().equals(ipAddress)) {
            log.warn("IP 주소 변경 감지: userId={}, 기존 IP={}, 새로운 IP={}",
                    userId, sessionInfo.getIpAddress(), ipAddress);
            // 세션 무효화 또는 알림 발송 만들 예정
        }

        sessionInfo.updateLastAccessTime();
        return true;
    }

    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.debug("세션 삭제: userId={}", userId);
    }

  
    public boolean checkConcurrentLogin(Long userId) {
        // 설정에 따라 동시 로그인 제한 여부 확인 만들예정
        
        return true;
    }

    public List<String> getActiveSessions(Long userId) {
        // 실제 세션 ID 목록 반환 기능 예정
        return List.of();
    }

    public void terminateSession(Long userId, String sessionId) {
        // 특정 세션 종료 기능예정
        removeSession(userId);
    }

    public void terminateAllSessions(Long userId) {
        removeSession(userId);
    }

    private static class SessionInfo {
        private final Long userId;
        private final String ipAddress;
        private long lastAccessTime;

        public SessionInfo(Long userId, String ipAddress, long lastAccessTime) {
            this.userId = userId;
            this.ipAddress = ipAddress;
            this.lastAccessTime = lastAccessTime;
        }

        public Long getUserId() {
            return userId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}

