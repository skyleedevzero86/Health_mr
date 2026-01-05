package com.sleekydz86.support.board.strategy;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import org.springframework.stereotype.Component;

@Component
public class DepartmentBoardPermissionStrategy implements BoardPermissionStrategy {

    @Override
    public boolean supports(BoardType boardType) {
        return boardType == BoardType.DEPARTMENT;
    }

    @Override
    public boolean canCreate(UserEntity user, Long departmentId) {
        if (user.getDepartment() == null) {
            return false;
        }
        return user.getDepartment().getId().equals(departmentId);
    }

    @Override
    public boolean canRead(BoardEntity board, UserEntity user, Long departmentId) {
        if (user.getRole() == RoleType.ADMIN) {
            return true;
        }
        if (user.getDepartment() == null) {
            return false;
        }
        return board.belongsToDepartment(user.getDepartment().getId());
    }

    @Override
    public boolean canUpdate(BoardEntity board, UserEntity user) {
        if (user.getRole() == RoleType.ADMIN) {
            return true;
        }
        return board.isWrittenBy(user.getId()) && canRead(board, user, user.getDepartment() != null ? user.getDepartment().getId() : null);
    }

    @Override
    public boolean canDelete(BoardEntity board, UserEntity user) {
        if (user.getRole() == RoleType.ADMIN) {
            return true;
        }
        return board.isWrittenBy(user.getId()) && canRead(board, user, user.getDepartment() != null ? user.getDepartment().getId() : null);
    }
}

