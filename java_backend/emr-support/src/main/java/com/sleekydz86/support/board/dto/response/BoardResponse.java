package com.sleekydz86.support.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private Long boardId;
    private Long authorId;
    private String authorName;
    private Long departmentId;
    private String departmentName;
    private BoardType boardType;
    private NoticeType noticeType;
    private String title;
    private String content;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long fileCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<String> hashtags;

    public static BoardResponse from(BoardEntity entity, List<String> hashtags, Long fileCount) {
        return new BoardResponse(
                entity.getBoardId(),
                entity.getAuthor().getId(),
                entity.getAuthor().getName(),
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.getBoardType(),
                entity.getNoticeType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getViewCount(),
                entity.getLikeCount(),
                entity.getCommentCount(),
                fileCount != null ? fileCount : 0L,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                hashtags
        );
    }
}

