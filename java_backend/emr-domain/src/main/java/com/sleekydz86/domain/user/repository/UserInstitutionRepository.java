package com.sleekydz86.domain.user.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.user.entity.UserInstitution;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInstitutionRepository extends BaseRepository<UserInstitution, Long> {

    @Query("SELECT ui FROM UserInstitution ui WHERE ui.user.id = :userId")
    List<UserInstitution> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ui.institution.institutionCode FROM UserInstitution ui WHERE ui.user.id = :userId")
    List<String> findInstitutionCodesByUserId(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserInstitution ui WHERE ui.user.id = :userId AND ui.isPrimary = true")
    Optional<UserInstitution> findPrimaryByUserId(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserInstitution ui WHERE ui.user.id = :userId AND ui.institution.institutionId = :institutionId")
    Optional<UserInstitution> findByUserIdAndInstitutionId(
            @Param("userId") Long userId,
            @Param("institutionId") Long institutionId
    );

    @Query("SELECT ui FROM UserInstitution ui WHERE ui.user.id = :userId AND ui.institution.institutionCode = :institutionCode")
    Optional<UserInstitution> findByUserIdAndInstitutionCode(
            @Param("userId") Long userId,
            @Param("institutionCode") String institutionCode
    );

    @Query("SELECT COUNT(ui) FROM UserInstitution ui WHERE ui.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserInstitution ui WHERE ui.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserInstitution ui WHERE ui.user.id = :userId AND ui.institution.institutionId = :institutionId")
    void deleteByUserIdAndInstitutionId(@Param("userId") Long userId, @Param("institutionId") Long institutionId);
}

