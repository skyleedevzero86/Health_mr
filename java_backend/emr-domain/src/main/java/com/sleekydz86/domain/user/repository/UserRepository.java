package com.sleekydz86.domain.user.repository;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM User u WHERE u.loginId.value = :loginId")
    Optional<UserEntity> findByLoginId(@Param("loginId") String loginId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.loginId.value = :loginId")
    boolean existsByLoginId(@Param("loginId") String loginId);

    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email.value = :email")
    boolean existsByEmail(@Param("email") String email);

    List<UserEntity> findAllByRole(RoleType role);

    Page<UserEntity> findAllByRole(RoleType role, Pageable pageable);

    List<UserEntity> findByRoleIn(List<RoleType> roles);

    List<UserEntity> findByDepartmentId(Long departmentId);

    List<UserEntity> findAllByOrderByRegisterDateDesc();

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<UserEntity> findByNameContaining(@Param("name") String name);

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") Long departmentId);
}

