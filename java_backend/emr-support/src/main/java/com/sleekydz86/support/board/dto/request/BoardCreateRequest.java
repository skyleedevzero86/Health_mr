package com.sleekydz86.support.board.dto.request;

import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {

    @NotNull(message = "게시판 타입은 필수 값입니다.")
    private BoardType boardType;

    private NoticeType noticeType;

    private Long departmentId;

    @NotBlank(message = "제목은 필수 값입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 필수 값입니다.")
    private String content;

    private List<String> hashtags;
}

