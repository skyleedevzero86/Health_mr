package com.sleekydz86.support.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.support.board.entity.BoardCommentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long commentId;
    private Long boardId;
    private Long authorId;
    private String authorName;
    private Long parentCommentId;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<CommentResponse> replies;

    public static CommentResponse from(BoardCommentEntity entity) {
        return new CommentResponse(
                entity.getCommentId(),
                entity.getBoard().getBoardId(),
                entity.getAuthor().getId(),
                entity.getAuthor().getName(),
                entity.getParentComment() != null ? entity.getParentComment().getCommentId() : null,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                List.of()
        );
    }

    public static CommentResponse from(BoardCommentEntity entity, List<CommentResponse> replies) {
        return new CommentResponse(
                entity.getCommentId(),
                entity.getBoard().getBoardId(),
                entity.getAuthor().getId(),
                entity.getAuthor().getName(),
                entity.getParentComment() != null ? entity.getParentComment().getCommentId() : null,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                replies
        );
    }
}

