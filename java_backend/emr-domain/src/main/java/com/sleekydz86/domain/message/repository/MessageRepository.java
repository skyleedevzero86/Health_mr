package com.sleekydz86.domain.message.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.message.type.MessageStatus;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends BaseRepository<MessageEntity, Long> {

    Optional<MessageEntity> findByMessageId(Long messageId);

    List<MessageEntity> findBySender_Id(Long senderId);

    List<MessageEntity> findByReceiver_Id(Long receiverId);

    List<MessageEntity> findBySender_IdAndSenderDeletedFalse(Long senderId);

    List<MessageEntity> findByReceiver_IdAndReceiverDeletedFalse(Long receiverId);

    List<MessageEntity> findBySender_IdAndStatus(Long senderId, MessageStatus status);

    List<MessageEntity> findByReceiver_IdAndStatus(Long receiverId, MessageStatus status);

    List<MessageEntity> findBySender_IdOrReceiver_Id(Long userId1, Long userId2);
}

