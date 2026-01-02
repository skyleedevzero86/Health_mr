package com.sleekydz86.core.security.jwt;

import java.util.List;

public interface TenantResolver {
    List<String> findInstitutionCodesByUserId(Long userId);
}
