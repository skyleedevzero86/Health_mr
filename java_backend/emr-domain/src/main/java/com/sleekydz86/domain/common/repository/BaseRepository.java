package com.sleekydz86.domain.common.repository;

import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {


    default Optional<T> findByIdAndDeletedFalse(ID id) {
        return findById(id).filter(entity -> {

            try {
                var deletedField = entity.getClass().getDeclaredField("deleted");
                deletedField.setAccessible(true);
                Boolean deleted = (Boolean) deletedField.get(entity);
                return deleted == null || !deleted;
            } catch (NoSuchFieldException | IllegalAccessException e) {

                return true;
            }
        });
    }

    default Optional<T> findByIdWithTenantFilter(ID id) {
        Optional<T> entity = findById(id);
        if (entity.isEmpty()) {
            return entity;
        }

        if (TenantContext.isAdmin()) {
            return entity;
        }

        if (TenantContext.shouldFilterByTenant() && entity.get() instanceof BaseEntity baseEntity) {
            if (TenantContext.belongsToTenant(baseEntity.getInttCd())) {
                return entity;
            }
            return Optional.empty();
        }

        return entity;
    }


    default List<T> findAllWithTenantFilter() {
        List<T> all = findAll();

        if (TenantContext.isAdmin()) {
            return all;
        }

        if (TenantContext.shouldFilterByTenant()) {
            return all.stream()
                    .filter(entity -> entity instanceof BaseEntity baseEntity &&
                            TenantContext.belongsToTenant(baseEntity.getInttCd()))
                    .toList();
        }

        return all;
    }
}

