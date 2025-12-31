package com.sleekydz86.core.tenant;

import java.util.List;

public class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> TENANT_IDS = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_ADMIN = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void setTenantIds(List<String> tenantIds) {
        TENANT_IDS.set(tenantIds);
    }

    public static List<String> getTenantIds() {
        return TENANT_IDS.get();
    }

    public static void setAdmin(boolean isAdmin) {
        IS_ADMIN.set(isAdmin);
    }

    public static boolean isAdmin() {
        return Boolean.TRUE.equals(IS_ADMIN.get());
    }

    public static void clear() {
        TENANT_ID.remove();
        TENANT_IDS.remove();
        IS_ADMIN.remove();
    }

    public static boolean shouldFilterByTenant() {
        return !isAdmin() && (getTenantId() != null || (getTenantIds() != null && !getTenantIds().isEmpty()));
    }

    public static boolean belongsToTenant(String institutionCode) {
        if (isAdmin()) {
            return true;
        }

        List<String> tenantIds = getTenantIds();
        if (tenantIds != null && !tenantIds.isEmpty()) {
            return tenantIds.contains(institutionCode);
        }

        String tenantId = getTenantId();
        return tenantId != null && tenantId.equals(institutionCode);
    }
}

