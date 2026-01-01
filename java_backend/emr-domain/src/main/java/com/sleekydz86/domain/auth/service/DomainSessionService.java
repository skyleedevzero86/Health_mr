package com.sleekydz86.domain.auth.service;

import com.sleekydz86.core.security.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainSessionService {

    private final SessionService coreSessionService;

    public boolean checkConcurrentLogin(Long userId) {
        return coreSessionService.checkConcurrentLogin(userId);
    }

    public List<String> getActiveSessions(Long userId) {
        return coreSessionService.getActiveSessions(userId);
    }

    @Transactional
    public void terminateSession(Long userId, String sessionId) {
        coreSessionService.terminateSession(userId, sessionId);
    }

    @Transactional
    public void terminateAllSessions(Long userId) {
        coreSessionService.terminateAllSessions(userId);
    }
}