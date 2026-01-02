package com.sleekydz86.domain.auth.filter;

import com.sleekydz86.core.security.jwt.TenantResolver;
import com.sleekydz86.domain.user.repository.UserInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantResolverImpl implements TenantResolver {

    private final UserInstitutionRepository userInstitutionRepository;

    @Override
    public List<String> findInstitutionCodesByUserId(Long userId) {
        return userInstitutionRepository.findInstitutionCodesByUserId(userId);
    }
}

