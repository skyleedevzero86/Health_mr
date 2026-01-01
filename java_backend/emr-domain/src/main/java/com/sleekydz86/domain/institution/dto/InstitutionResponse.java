package com.sleekydz86.domain.institution.dto;

import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class InstitutionResponse {

    private Long institutionId;
    private String institutionCode;
    private String institutionName;
    private String institutionEngName;
    private String institutionAddress;
    private String institutionTel;
    private String institutionEmail;
    private String directorName;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    private InstitutionResponse(
            Long institutionId,
            String institutionCode,
            String institutionName,
            String institutionEngName,
            String institutionAddress,
            String institutionTel,
            String institutionEmail,
            String directorName,
            Boolean isActive,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.institutionId = institutionId;
        this.institutionCode = institutionCode;
        this.institutionName = institutionName;
        this.institutionEngName = institutionEngName;
        this.institutionAddress = institutionAddress;
        this.institutionTel = institutionTel;
        this.institutionEmail = institutionEmail;
        this.directorName = directorName;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static InstitutionResponse from(InstitutionEntity entity) {
        return new InstitutionResponse(
                entity.getInstitutionId(),
                entity.getInstitutionCode(),
                entity.getInstitutionName(),
                entity.getInstitutionEngName(),
                entity.getInstitutionAddress(),
                entity.getInstitutionTel(),
                entity.getInstitutionEmail(),
                entity.getDirectorName(),
                entity.isActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }
}

