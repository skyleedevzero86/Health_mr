package com.sleekydz86.emrclinical.diagnosis.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kcd_code", indexes = {
        @Index(name = "idx_kcd_code", columnList = "code"),
        @Index(name = "idx_kcd_name_korean", columnList = "nameKorean")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KcdCodeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String nameKorean;

    @Column(length = 200)
    private String nameEnglish;

    @Column(length = 20)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
