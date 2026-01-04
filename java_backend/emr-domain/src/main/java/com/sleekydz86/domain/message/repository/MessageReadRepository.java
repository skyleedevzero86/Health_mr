package com.sleekydz86.domain.message.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.message.entity.MessageReadEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReadRepository extends BaseRepository<MessageReadEntity, Long> {

    Optional<MessageReadEntity> findByReadId(Long readId);

    Optional<MessageReadEntity> findByMessage_MessageIdAndUser_Id(Long messageId, Long userId);

    List<MessageReadEntity> findByMessage_MessageId(Long messageId);

    List<MessageReadEntity> findByUser_Id(Long userId);
}

