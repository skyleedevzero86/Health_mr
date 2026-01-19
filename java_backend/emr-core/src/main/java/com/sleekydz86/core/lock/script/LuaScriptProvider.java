package com.sleekydz86.core.lock.script;

import org.springframework.stereotype.Component;

@Component
public class LuaScriptProvider {

    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    private static final String EXTEND_LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
        "else " +
        "    return 0 " +
        "end";

    public String getUnlockScript() {
        return UNLOCK_SCRIPT;
    }

    public String getExtendLockScript() {
        return EXTEND_LOCK_SCRIPT;
    }
}
