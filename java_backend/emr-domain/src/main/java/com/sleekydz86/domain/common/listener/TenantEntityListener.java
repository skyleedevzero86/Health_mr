package com.sleekydz86.domain.common.listener;

import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {

            if (baseEntity.getInttCd() == null || baseEntity.getInttCd().isBlank()) {

                if (!TenantContext.isAdmin() && TenantContext.getTenantId() != null) {
                    baseEntity.setInttCd(TenantContext.getTenantId());
                    log.debug("엔티티 저장 시 inttCd 자동 설정: {}", TenantContext.getTenantId());
                }
            }
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {

            if (TenantContext.isAdmin()) {
                return;
            }

            String currentTenantId = TenantContext.getTenantId();
            String entityTenantId = baseEntity.getInttCd();

            if (currentTenantId != null && entityTenantId != null && !currentTenantId.equals(entityTenantId)) {
                throw new IllegalStateException("다른 기관의 데이터는 수정할 수 없습니다.");
            }
        }
    }
}
