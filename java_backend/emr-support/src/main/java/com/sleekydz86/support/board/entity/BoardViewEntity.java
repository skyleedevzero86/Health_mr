package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "BoardView")
@Table(name = "board_view", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"board_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardViewEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id", nullable = false)
    private Long viewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Builder
    private BoardViewEntity(
            Long viewId,
            BoardEntity board,
            UserEntity user,
            LocalDateTime viewedAt
    ) {
        this.viewId = viewId;
        this.board = board;
        this.user = user;
        this.viewedAt = viewedAt != null ? viewedAt : LocalDateTime.now();
    }
}

