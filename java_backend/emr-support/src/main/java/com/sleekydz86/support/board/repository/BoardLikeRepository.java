package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.board.entity.BoardLikeEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardLikeRepository extends BaseRepository<BoardLikeEntity, Long> {

    Optional<BoardLikeEntity> findByLikeId(Long likeId);

    Optional<BoardLikeEntity> findByBoard_BoardIdAndUser_Id(Long boardId, Long userId);

    List<BoardLikeEntity> findByBoard_BoardId(Long boardId);

    List<BoardLikeEntity> findByUser_Id(Long userId);

    boolean existsByBoard_BoardIdAndUser_Id(Long boardId, Long userId);
}

