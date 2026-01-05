package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.board.entity.BoardCommentEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCommentRepository extends BaseRepository<BoardCommentEntity, Long> {

    Optional<BoardCommentEntity> findByCommentId(Long commentId);

    List<BoardCommentEntity> findByBoard_BoardIdAndDeletedFalseOrderByCreatedAtAsc(Long boardId);

    List<BoardCommentEntity> findByParentComment_CommentIdAndDeletedFalse(Long parentCommentId);

    List<BoardCommentEntity> findByAuthor_IdAndDeletedFalse(Long userId);
}

