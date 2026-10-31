package com.sleekydz86.emrclinical.checkin.service;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.service.DepartmentService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.repository.CheckInRepository;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInService implements BaseService<CheckInEntity, Long> {

    private final CheckInRepository checkInRepository;
    private final PatientService patientService;
    private final UserService userService;
    private final DepartmentService departmentService;

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
        DepartmentEntity department = user != null ? user.getDepartment() : null;
        if (department == null) {
            throw new BusinessException("예약 담당자의 진료과가 없어 접수할 수 없습니다.");
        }
        if (checkInRepository.existsActiveByPatientDayAndDepartment(
                patient.getPatientNoValue(), LocalDate.now(), department.getId())) {
            throw new DuplicateException("이미 같은 날 같은 진료과로 접수된 환자입니다.");
        }

        CheckInEntity checkIn = CheckInEntity.builder()
                .patientEntity(patient)
                .userEntity(user)
                .departmentEntity(department)
                .checkInDate(LocalDateTime.now())
                .checkInStatus(CheckInStatus.PENDING)
                .checkInComment("예약으로부터 생성된 접수")
                .build();

        return saveCheckIn(checkIn);
    }

    @Transactional
    public CheckInEntity createCheckIn(Long patientNo, Long userId, Long departmentId) {
        if (departmentId == null) {
            throw new BusinessException("진료과는 필수입니다.");
        }
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        UserEntity user = userId != null ? userService.getUserById(userId) : null;
        DepartmentEntity department = departmentService.getDepartmentById(departmentId);
        LocalDate day = LocalDate.now();
        if (checkInRepository.existsActiveByPatientDayAndDepartment(patientNo, day, departmentId)) {
            throw new DuplicateException("이미 같은 날 같은 진료과로 접수된 환자입니다.");
        }

        CheckInEntity checkIn = CheckInEntity.builder()
                .patientEntity(patient)
                .userEntity(user)
                .departmentEntity(department)
                .checkInDate(LocalDateTime.now())
                .checkInStatus(CheckInStatus.PENDING)
                .build();

        return saveCheckIn(checkIn);
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

    private CheckInEntity saveCheckIn(CheckInEntity checkIn) {
        try {
            return checkInRepository.saveAndFlush(checkIn);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateException("이미 같은 날 같은 진료과로 접수된 환자입니다.", exception);
        }
    }
}
