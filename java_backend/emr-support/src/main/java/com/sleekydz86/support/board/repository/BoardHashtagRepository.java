package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.board.entity.BoardHashtagEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardHashtagRepository extends BaseRepository<BoardHashtagEntity, Long> {

    Optional<BoardHashtagEntity> findByHashtagId(Long hashtagId);

    List<BoardHashtagEntity> findByBoard_BoardId(Long boardId);

    @Query("SELECT h FROM BoardHashtag h WHERE h.hashtag.value = :tag")
    List<BoardHashtagEntity> findByHashtag_Tag(@Param("tag") String tag);
}

