package com.sleekydz86.support.board.strategy;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;

public interface BoardPermissionStrategy {
    boolean supports(BoardType boardType);
    boolean canCreate(UserEntity user, Long departmentId);
    boolean canRead(BoardEntity board, UserEntity user, Long departmentId);
    boolean canUpdate(BoardEntity board, UserEntity user);
    boolean canDelete(BoardEntity board, UserEntity user);
}

