package com.sleekydz86.support.equipment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity(name = "Equipment_Journal")
@Table(name = "equipment_journal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EquipmentJournalEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_journal_id", nullable = false)
    private Long equipmentJournalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id", nullable = false)
    private EquipmentEntity equipmentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "equipment_inspection_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate equipmentInspectionDate;

    @Column(name = "equipment_inspection_result", columnDefinition = "TEXT")
    private String equipmentInspectionResult;

    @Column(name = "equipment_inspection_records", columnDefinition = "TEXT")
    private String equipmentInspectionRecords;

    @Column(name = "equipment_inspection_notes", columnDefinition = "TEXT")
    private String equipmentInspectionNotes;
}

