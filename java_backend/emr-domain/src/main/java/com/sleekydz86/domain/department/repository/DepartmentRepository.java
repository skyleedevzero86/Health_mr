package com.sleekydz86.domain.department.repository;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.type.DepartmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    Optional<DepartmentEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<DepartmentEntity> findByType(DepartmentType type);

    List<DepartmentEntity> findByNameContaining(String name);

    Page<DepartmentEntity> findAll(Pageable pageable);

    List<DepartmentEntity> findAllByOrderByNameAsc();
}
