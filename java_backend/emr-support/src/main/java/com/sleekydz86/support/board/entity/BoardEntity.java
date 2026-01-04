package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Board")
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 20)
    private BoardType boardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", length = 20)
    private NoticeType noticeType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Builder
    private BoardEntity(
            Long boardId,
            UserEntity author,
            DepartmentEntity department,
            BoardType boardType,
            NoticeType noticeType,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
        this.boardId = boardId;
        this.author = author;
        this.department = department;
        this.boardType = boardType;
        this.noticeType = noticeType;
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = null;
        this.deleted = false;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public boolean isWrittenBy(Long userId) {
        return this.author != null && this.author.getId().equals(userId);
    }

    public boolean belongsToDepartment(Long departmentId) {
        return this.department != null && this.department.getId().equals(departmentId);
    }
}

