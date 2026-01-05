package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "BoardLike")
@Table(name = "board_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"board_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLikeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id", nullable = false)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private BoardLikeEntity(
            Long likeId,
            BoardEntity board,
            UserEntity user,
            LocalDateTime createdAt
    ) {
        this.likeId = likeId;
        this.board = board;
        this.user = user;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public boolean isLikedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }
}

