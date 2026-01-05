package com.sleekydz86.support.board.strategy;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import org.springframework.stereotype.Component;

@Component
public class GeneralBoardPermissionStrategy implements BoardPermissionStrategy {

    @Override
    public boolean supports(BoardType boardType) {
        return boardType == BoardType.GENERAL;
    }

    @Override
    public boolean canCreate(UserEntity user, Long departmentId) {
        return true;
    }

    @Override
    public boolean canRead(BoardEntity board, UserEntity user, Long departmentId) {
        return true;
    }

    @Override
    public boolean canUpdate(BoardEntity board, UserEntity user) {
        if (user.getRole() == RoleType.ADMIN) {
            return true;
        }
        return board.isWrittenBy(user.getId());
    }

    @Override
    public boolean canDelete(BoardEntity board, UserEntity user) {
        if (user.getRole() == RoleType.ADMIN) {
            return true;
        }
        return board.isWrittenBy(user.getId());
    }
}

