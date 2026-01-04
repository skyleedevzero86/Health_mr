package com.sleekydz86.support.recess.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.recess.entity.RecessEntity;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface RecessRepository extends BaseRepository<RecessEntity, Long> {

    List<RecessEntity> findByUserEntity_Role(RoleType roleType);
}

