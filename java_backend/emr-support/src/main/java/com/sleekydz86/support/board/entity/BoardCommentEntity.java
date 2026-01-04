package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "BoardComment")
@Table(name = "board_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardCommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private BoardCommentEntity parentComment;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Builder
    private BoardCommentEntity(
            Long commentId,
            BoardEntity board,
            UserEntity author,
            BoardCommentEntity parentComment,
            String content,
            LocalDateTime createdAt
    ) {
        this.commentId = commentId;
        this.board = board;
        this.author = author;
        this.parentComment = parentComment;
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = null;
        this.deleted = false;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isWrittenBy(Long userId) {
        return this.author != null && this.author.getId().equals(userId);
    }

    public boolean isReply() {
        return this.parentComment != null;
    }
}

