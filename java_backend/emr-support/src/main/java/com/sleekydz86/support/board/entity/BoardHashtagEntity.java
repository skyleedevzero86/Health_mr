package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.support.board.valueobject.Hashtag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "BoardHashtag")
@Table(name = "board_hashtag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardHashtagEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id", nullable = false)
    private Long hashtagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "tag", nullable = false, length = 50))
    private Hashtag hashtag;

    @Builder
    private BoardHashtagEntity(
            Long hashtagId,
            BoardEntity board,
            Hashtag hashtag
    ) {
        this.hashtagId = hashtagId;
        this.board = board;
        this.hashtag = hashtag;
    }
}

