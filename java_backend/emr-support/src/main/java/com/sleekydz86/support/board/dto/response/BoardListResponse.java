package com.sleekydz86.support.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardListResponse {

    private Long totalCount;
    private List<BoardResponse> boards;

    public static BoardListResponse of(List<BoardResponse> boards) {
        return new BoardListResponse((long) boards.size(), boards);
    }
}

