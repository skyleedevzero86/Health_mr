package com.sleekydz86.domain.common.service;


import com.sleekydz86.core.common.exception.custom.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseService<T, ID> {

    default T validateExists(JpaRepository<T, ID> repository, ID id, String errorMessage) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(errorMessage));
    }

    default T validateExists(JpaRepository<T, ID> repository, ID id) {
        return validateExists(repository, id, "엔티티를 찾을 수 없습니다. ID: " + id);
    }

    default void validateNotDuplicate(boolean condition, String errorMessage) {
        if (condition) {
            throw new com.sleekydz86.core.common.exception.custom.DuplicateException(errorMessage);
        }
    }
}

