package com.sleekydz86.support.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.board.entity.BoardViewEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardViewerResponse {

    private Long viewId;
    private Long userId;
    private String userName;
    private String departmentName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime viewedAt;

    public static BoardViewerResponse from(BoardViewEntity entity) {
        return new BoardViewerResponse(
                entity.getViewId(),
                entity.getUser().getId(),
                entity.getUser().getName(),
                entity.getUser().getDepartment() != null ? entity.getUser().getDepartment().getName() : null,
                entity.getViewedAt()
        );
    }
}

