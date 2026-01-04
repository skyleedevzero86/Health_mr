package com.sleekydz86.support.board.service.domain;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import org.springframework.stereotype.Service;

@Service
public class BoardDomainService {

    public void validateBoardCreate(UserEntity author, BoardType boardType, Long departmentId) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }

        if (boardType == BoardType.NOTICE && author.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("공지게시판은 관리자만 작성할 수 있습니다.");
        }

        if (boardType == BoardType.DEPARTMENT) {
            if (departmentId == null) {
                throw new IllegalArgumentException("부서별 게시판은 부서 정보가 필요합니다.");
            }
            if (author.getDepartment() == null || !author.getDepartment().getId().equals(departmentId)) {
                throw new IllegalArgumentException("본인 부서의 게시판만 작성할 수 있습니다.");
            }
        }
    }

    public boolean canUpdateBoard(BoardEntity board, Long userId, RoleType userRole) {
        if (board.isWrittenBy(userId)) {
            return true;
        }
        return userRole == RoleType.ADMIN;
    }

    public boolean canDeleteBoard(BoardEntity board, Long userId, RoleType userRole) {
        if (board.isWrittenBy(userId)) {
            return true;
        }
        return userRole == RoleType.ADMIN;
    }

    public boolean canReadBoard(BoardEntity board, Long userId, RoleType userRole, Long departmentId) {
        if (board.getBoardType() == BoardType.NOTICE) {
            return true;
        }

        if (board.getBoardType() == BoardType.DEPARTMENT) {
            if (userRole == RoleType.ADMIN) {
                return true;
            }
            return board.belongsToDepartment(departmentId);
        }

        return true;
    }
}

