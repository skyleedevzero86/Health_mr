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
        }

        sessionInfo.updateLastAccessTime();
        return true;
    }

    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.debug("세션 삭제: userId={}", userId);
    }

    public boolean checkConcurrentLogin(Long userId) {
        return true;
    }

    public List<String> getActiveSessions(Long userId) {
        return List.of();
    }

    public void terminateSession(Long userId, String sessionId) {
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

