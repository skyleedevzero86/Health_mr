package com.sleekydz86.support.board.dto.response;

import com.sleekydz86.support.board.entity.BoardFileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardFileResponse {

    private Long fileId;
    private String originalFilename;
    private String savedFilename;
    private Long fileSize;
    private String contentType;

    public static BoardFileResponse from(BoardFileEntity entity) {
        return new BoardFileResponse(
                entity.getFileId(),
                entity.getOriginalFilename(),
                entity.getSavedFilename(),
                entity.getFileSize(),
                entity.getContentType()
        );
    }
}

