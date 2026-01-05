package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.board.entity.BoardFileEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardFileRepository extends BaseRepository<BoardFileEntity, Long> {

    Optional<BoardFileEntity> findByFileId(Long fileId);

    List<BoardFileEntity> findByBoard_BoardIdAndDeletedFalse(Long boardId);

    long countByBoard_BoardIdAndDeletedFalse(Long boardId);
}

