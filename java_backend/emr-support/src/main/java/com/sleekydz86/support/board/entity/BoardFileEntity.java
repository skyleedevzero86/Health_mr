package com.sleekydz86.support.board.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "BoardFile")
@Table(name = "board_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardFileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "saved_filename", nullable = false, length = 255)
    private String savedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Builder
    private BoardFileEntity(
            Long fileId,
            BoardEntity board,
            String originalFilename,
            String savedFilename,
            String filePath,
            Long fileSize,
            String contentType
    ) {
        this.fileId = fileId;
        this.board = board;
        this.originalFilename = originalFilename;
        this.savedFilename = savedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.deleted = false;
    }

    public void delete() {
        this.deleted = true;
    }
}

