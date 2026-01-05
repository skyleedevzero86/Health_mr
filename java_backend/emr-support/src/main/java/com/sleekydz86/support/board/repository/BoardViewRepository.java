package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.board.entity.BoardViewEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardViewRepository extends BaseRepository<BoardViewEntity, Long> {

    Optional<BoardViewEntity> findByViewId(Long viewId);

    Optional<BoardViewEntity> findByBoard_BoardIdAndUser_Id(Long boardId, Long userId);

    List<BoardViewEntity> findByBoard_BoardIdOrderByViewedAtDesc(Long boardId);

    boolean existsByBoard_BoardIdAndUser_Id(Long boardId, Long userId);
}

