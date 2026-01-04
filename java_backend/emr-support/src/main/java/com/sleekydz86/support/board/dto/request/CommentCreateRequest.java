package com.sleekydz86.support.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수 값입니다.")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
    private String content;

    private Long parentCommentId;
}

