package com.sleekydz86.support.board.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends BaseRepository<BoardEntity, Long> {

    Optional<BoardEntity> findByBoardId(Long boardId);

    Page<BoardEntity> findByBoardTypeAndDeletedFalse(BoardType boardType, Pageable pageable);

    Page<BoardEntity> findByBoardTypeAndDepartmentAndDeletedFalse(
            BoardType boardType, DepartmentEntity department, Pageable pageable);

    Page<BoardEntity> findByDeletedFalse(Pageable pageable);

    List<BoardEntity> findByAuthor_IdAndDeletedFalse(Long userId);

    List<BoardEntity> findByDepartment_IdAndDeletedFalse(Long departmentId);

    Page<BoardEntity> findByBoardTypeAndNoticeTypeAndDeletedFalse(
            BoardType boardType, NoticeType noticeType, Pageable pageable);
}

