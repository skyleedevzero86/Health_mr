package com.sleekydz86.emrclinical.diagnosis.dto;

import com.sleekydz86.emrclinical.diagnosis.entity.KcdCodeEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KcdCodeResponse {

    private Long id;
    private String code;
    private String nameKorean;
    private String nameEnglish;
    private String category;

    public static KcdCodeResponse from(KcdCodeEntity entity) {
        return KcdCodeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .nameKorean(entity.getNameKorean())
                .nameEnglish(entity.getNameEnglish())
                .category(entity.getCategory())
                .build();
    }
}
