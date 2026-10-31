package com.sleekydz86.emrclinical.checkin.service;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.repository.CheckInRepository;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInService implements BaseService<CheckInEntity, Long> {

    private final CheckInRepository checkInRepository;
    private final PatientService patientService;
    private final UserService userService;

    public CheckInEntity getById(Long id) {
        return validateExists(checkInRepository, id, "접수를 찾을 수 없습니다. ID: " + id);
    }

    public CheckInEntity getCheckInById(Long checkInId) {
        return checkInRepository.findDetailById(checkInId)
                .or(() -> checkInRepository.findByCheckInId(checkInId))
                .orElseThrow(() -> new NotFoundException("접수를 찾을 수 없습니다. ID: " + checkInId));
    }

    @Transactional
    public CheckInEntity createCheckInFromReservation(ReservationEntity reservation) {
        PatientEntity patient = reservation.getPatientEntity();
        UserEntity user = reservation.getUserEntity();

        CheckInEntity checkIn = CheckInEntity.builder()
                .patientEntity(patient)
                .userEntity(user)
                .checkInDate(LocalDateTime.now())
                .checkInStatus(CheckInStatus.PENDING)
                .checkInComment("예약으로부터 생성된 접수")
                .build();

        return checkInRepository.save(checkIn);
    }

    @Transactional
    public CheckInEntity createCheckIn(Long patientNo, Long userId) {
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        UserEntity user = userId != null ? userService.getUserById(userId) : null;

        CheckInEntity checkIn = CheckInEntity.builder()
                .patientEntity(patient)
                .userEntity(user)
                .checkInDate(LocalDateTime.now())
                .checkInStatus(CheckInStatus.PENDING)
                .build();

        return checkInRepository.save(checkIn);
    }

    public List<CheckInEntity> getCheckInsByPatientNo(Long patientNo) {
        patientService.getPatientByNo(patientNo);
        return checkInRepository.findByPatientEntity_PatientNo(patientNo);
    }

    @Transactional
    public CheckInEntity completeCheckIn(Long checkInId) {
        CheckInEntity checkIn = getCheckInById(checkInId);
        if (checkIn.getCheckInStatus() == CheckInStatus.COMPLETED) {
            throw new BusinessException("이미 완료된 접수입니다.");
        }
        if (checkIn.getCheckInStatus() == CheckInStatus.CANCELLED) {
            throw new BusinessException("취소된 접수는 완료할 수 없습니다.");
        }
        checkIn.complete();
        return checkInRepository.save(checkIn);
    }

    @Transactional
    public CheckInEntity cancelCheckIn(Long checkInId) {
        CheckInEntity checkIn = getCheckInById(checkInId);
        if (checkIn.getCheckInStatus() == CheckInStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 접수입니다.");
        }
        if (checkIn.getCheckInStatus() == CheckInStatus.COMPLETED) {
            throw new BusinessException("완료된 접수는 취소할 수 없습니다.");
        }
        checkIn.cancel();
        return checkInRepository.save(checkIn);
    }
}

