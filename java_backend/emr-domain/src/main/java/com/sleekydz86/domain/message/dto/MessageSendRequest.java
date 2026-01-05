package com.sleekydz86.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequest {

    @NotNull(message = "수신자 ID는 필수 값입니다.")
    private Long receiverId;

    @NotBlank(message = "제목은 필수 값입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String subject;

    @NotBlank(message = "내용은 필수 값입니다.")
    private String content;
}

