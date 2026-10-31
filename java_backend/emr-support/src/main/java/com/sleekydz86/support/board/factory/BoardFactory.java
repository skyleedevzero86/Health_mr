package com.sleekydz86.support.board.factory;

import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardFactory {

    private final List<com.sleekydz86.support.board.strategy.BoardPermissionStrategy> permissionStrategies;

    public BoardEntity create(
            UserEntity author,
            DepartmentEntity department,
            BoardType boardType,
            NoticeType noticeType,
            String title,
            String content
    ) {
        com.sleekydz86.support.board.strategy.BoardPermissionStrategy strategy = findStrategy(boardType);
        
        Long departmentId = department != null ? department.getId() : null;
        if (!strategy.canCreate(author, departmentId)) {
            throw new IllegalArgumentException("게시판 작성 권한이 없습니다.");
        }

        return BoardEntity.builder()
                .author(author)
                .department(department)
                .boardType(boardType)
                .noticeType(noticeType)
                .title(title)
                .content(content)
                .build();
    }

    private com.sleekydz86.support.board.strategy.BoardPermissionStrategy findStrategy(BoardType boardType) {
        return permissionStrategies.stream()
                .filter(strategy -> strategy.supports(boardType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 게시판 타입입니다: " + boardType));
    }
}

