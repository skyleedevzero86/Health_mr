package com.sleekydz86.support.equipment.journal.service;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.entity.EquipmentJournalEntity;
import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalRegisterRequest;
import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalResponse;
import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalUpdateRequest;
import com.sleekydz86.support.equipment.journal.repository.EquipmentJournalRepository;
import com.sleekydz86.support.equipment.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentJournalService {

    private final EquipmentJournalRepository journalRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public EquipmentJournalResponse registerJournal(EquipmentJournalRegisterRequest request) {

        EquipmentEntity equipment = equipmentRepository.findByEquipmentId(request.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("장비 정보를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        EquipmentJournalEntity journal = EquipmentJournalEntity.builder()
                .equipmentEntity(equipment)
                .userEntity(user)
                .equipmentInspectionDate(request.getEquipmentInspectionDate())
                .equipmentInspectionResult(request.getEquipmentInspectionResult())
                .equipmentInspectionRecords(request.getEquipmentInspectionRecords())
                .equipmentInspectionNotes(request.getEquipmentInspectionNotes())
                .build();

        EquipmentJournalEntity saved = journalRepository.save(journal);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EquipmentJournalResponse getJournal(Long journalId) {
        EquipmentJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("점검 일지를 찾을 수 없습니다."));
        return toResponse(journal);
    }

    @Transactional(readOnly = true)
    public List<EquipmentJournalResponse> getJournalsByEquipment(Long equipmentId) {
        List<EquipmentJournalEntity> journals = journalRepository.findByEquipmentEntity_EquipmentId(equipmentId);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<EquipmentJournalResponse> getJournalsByUser(Long userId) {
        List<EquipmentJournalEntity> journals = journalRepository.findByUserEntity_Id(userId);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentJournalResponse> getJournalsByDate(LocalDate date) {
        List<EquipmentJournalEntity> journals = journalRepository.findByEquipmentInspectionDate(date);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentJournalResponse> getJournalsByEquipmentAndPeriod(Long equipmentId, LocalDate start, LocalDate end) {
        List<EquipmentJournalEntity> journals = journalRepository
                .findByEquipmentEntity_EquipmentIdAndEquipmentInspectionDateBetween(equipmentId, start, end);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentJournalResponse updateJournal(Long journalId, EquipmentJournalUpdateRequest request) {
        EquipmentJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("점검 일지를 찾을 수 없습니다."));

        EquipmentJournalEntity updated = EquipmentJournalEntity.builder()
                .equipmentJournalId(journal.getEquipmentJournalId())
                .equipmentEntity(journal.getEquipmentEntity())
                .userEntity(journal.getUserEntity())
                .equipmentInspectionDate(request.getEquipmentInspectionDate() != null
                        ? request.getEquipmentInspectionDate()
                        : journal.getEquipmentInspectionDate())
                .equipmentInspectionResult(request.getEquipmentInspectionResult() != null
                        ? request.getEquipmentInspectionResult()
                        : journal.getEquipmentInspectionResult())
                .equipmentInspectionRecords(request.getEquipmentInspectionRecords() != null
                        ? request.getEquipmentInspectionRecords()
                        : journal.getEquipmentInspectionRecords())
                .equipmentInspectionNotes(request.getEquipmentInspectionNotes() != null
                        ? request.getEquipmentInspectionNotes()
                        : journal.getEquipmentInspectionNotes())
                .build();

        EquipmentJournalEntity saved = journalRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public EquipmentJournalResponse deleteJournal(Long journalId) {
        EquipmentJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("점검 일지를 찾을 수 없습니다."));

        EquipmentJournalResponse response = toResponse(journal);
        journalRepository.delete(journal);
        return response;
    }

    private EquipmentJournalResponse toResponse(EquipmentJournalEntity journal) {
        EquipmentEntity equipment = journal.getEquipmentEntity();
        UserEntity user = journal.getUserEntity();

        return EquipmentJournalResponse.builder()
                .equipmentJournalId(journal.getEquipmentJournalId())
                .equipmentId(equipment.getEquipmentId())
                .equipmentName(equipment.getEquipmentName())
                .userId(user.getId())
                .userName(user.getName())
                .equipmentInspectionDate(journal.getEquipmentInspectionDate())
                .equipmentInspectionResult(journal.getEquipmentInspectionResult())
                .equipmentInspectionRecords(journal.getEquipmentInspectionRecords())
                .equipmentInspectionNotes(journal.getEquipmentInspectionNotes())
                .build();
    }
}