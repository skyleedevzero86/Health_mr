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
public class BoardDetailResponse {

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
    private List<CommentResponse> comments;
    private List<BoardFileResponse> files;
    private Boolean isLiked;

    public static BoardDetailResponse from(BoardEntity entity, List<String> hashtags, List<CommentResponse> comments, List<BoardFileResponse> files, Boolean isLiked) {
        return new BoardDetailResponse(
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
                files != null ? (long) files.size() : 0L,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                hashtags,
                comments,
                files != null ? files : List.of(),
                isLiked
        );
    }
}

