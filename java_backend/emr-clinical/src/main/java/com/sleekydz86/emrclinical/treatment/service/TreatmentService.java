package com.sleekydz86.emrclinical.treatment.service;

import com.sleekydz86.core.clinical.TreatmentPaymentGuard;
import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.domain.TreatmentCancelledEvent;
import com.sleekydz86.core.event.domain.TreatmentCompletedEvent;
import com.sleekydz86.core.event.domain.TreatmentCreatedEvent;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.service.DepartmentService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.service.CheckInService;
import com.sleekydz86.emrclinical.reservation.service.ReservationService;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCompleteRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCreateRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentUpdateRequest;
import com.sleekydz86.emrclinical.treatment.emergency.EmergencyTreatmentEntity;
import com.sleekydz86.emrclinical.treatment.emergency.EmergencyTreatmentRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.InTreatmentEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.InTreatmentRepository;
import com.sleekydz86.emrclinical.treatment.notification.TreatmentNotificationService;
import com.sleekydz86.emrclinical.treatment.outpatient.OutTreatmentEntity;
import com.sleekydz86.emrclinical.treatment.outpatient.OutTreatmentRepository;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreatmentService implements BaseService<TreatmentEntity, Long> {

    private final TreatmentRepository treatmentRepository;
    private final CheckInService checkInService;
    private final PatientService patientService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final ReservationService reservationService;
    private final EventPublisher eventPublisher;
    private final OutTreatmentRepository outTreatmentRepository;
    private final InTreatmentRepository inTreatmentRepository;
    private final EmergencyTreatmentRepository emergencyTreatmentRepository;
    private final TreatmentNotificationService treatmentNotificationService;
    private final ObjectProvider<TreatmentPaymentGuard> treatmentPaymentGuard;

    @Value("${clinical.integration.auto-complete-reservation-on-treatment-complete:true}")
    private boolean autoCompleteReservationOnTreatmentComplete;

    @Transactional
    public TreatmentEntity createTreatment(TreatmentCreateRequest request) {

        if (request.getCheckInId() == null) {
            throw new BusinessException("접수는 필수입니다.");
        }

        CheckInEntity checkIn = checkInService.getCheckInById(request.getCheckInId());
        if (checkIn.getCheckInStatus() != CheckInStatus.COMPLETED) {
            throw new BusinessException("완료된 접수만 진료를 생성할 수 있습니다.");
        }

        List<TreatmentEntity> existingTreatments = treatmentRepository.findByCheckInEntity_CheckInId(
                checkIn.getCheckInId());
        if (!existingTreatments.isEmpty()) {
            throw new DuplicateException("이미 해당 접수로 생성된 진료가 있습니다.");
        }

        PatientEntity patient = checkIn.getPatientEntity();
        if (patient == null) {
            throw new BusinessException("환자 정보는 필수입니다.");
        }
        if (request.getPatientNo() != null && !request.getPatientNo().equals(patient.getPatientNoValue())) {
            throw new BusinessException("접수 환자와 요청 환자가 다릅니다.");
        }

        DepartmentEntity department = checkIn.getDepartmentEntity();
        if (department == null) {
            throw new BusinessException("접수 진료과가 없습니다.");
        }
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(department.getId())) {
            throw new BusinessException("접수 진료과와 다릅니다.");
        }

        UserEntity doctor = resolveDoctor(request.getDoctorId());

        TreatmentEntity treatment = TreatmentEntity.builder()
                .treatmentType(request.getTreatmentType())
                .treatmentDate(LocalDateTime.now())
                .treatmentStatus(TreatmentStatus.PENDING)
                .treatmentDoc(doctor)
                .departmentEntity(department)
                .treatmentDept(department.getName())
                .checkInEntity(checkIn)
                .patientEntity(patient)
                .build();

        TreatmentEntity saved;
        try {
            saved = treatmentRepository.saveAndFlush(treatment);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateException("이미 해당 접수로 생성된 진료가 있습니다.", exception);
        }

        createTreatmentTypeEntity(saved, request.getTreatmentType(), checkIn);

        eventPublisher.publish(new TreatmentCreatedEvent(
                saved.getTreatmentId(),
                patient.getPatientNoValue(),
                patient.getPatientName(),
                saved.getTreatmentType().name(),
                saved.getTreatmentDate()
        ));

        return saved;
    }

    private void createTreatmentTypeEntity(TreatmentEntity treatment, TreatmentType type, CheckInEntity checkIn) {
        TreatmentEntity persisted = treatmentRepository.getReferenceById(treatment.getTreatmentId());
        switch (type) {
            case OUTPATIENT:
                OutTreatmentEntity outTreatment = new OutTreatmentEntity();
                outTreatment.setTreatmentId(persisted);
                outTreatment.setCheckInEntity(checkIn);
                outTreatment.setTreatmentStatus(TreatmentStatus.PENDING);
                outTreatmentRepository.save(outTreatment);
                break;
            case INPATIENT:
                InTreatmentEntity inTreatment = new InTreatmentEntity();
                inTreatment.setTreatmentId(persisted);
                inTreatment.setCheckInEntity(checkIn);
                inTreatment.setTreatmentStatus(TreatmentStatus.PENDING);
                inTreatmentRepository.save(inTreatment);
                break;
            case EMERGENCY:
                EmergencyTreatmentEntity emergencyTreatment = new EmergencyTreatmentEntity();
                emergencyTreatment.setTreatmentId(persisted);
                emergencyTreatment.setCheckInEntity(checkIn);
                emergencyTreatment.setTreatmentStatus(TreatmentStatus.PENDING);
                emergencyTreatmentRepository.save(emergencyTreatment);
                break;
        }
    }

    public TreatmentEntity getTreatmentById(Long treatmentId) {
        return treatmentRepository.findDetailById(treatmentId)
                .orElseGet(() -> validateExists(treatmentRepository, treatmentId, "진료를 찾을 수 없습니다. ID: " + treatmentId));
    }

    public Page<TreatmentEntity> getTreatmentsByPatientNo(Long patientNo, Pageable pageable) {

        patientService.getPatientByNo(patientNo);
        return treatmentRepository.findByPatientEntity_PatientNo(patientNo, pageable);
    }

    public Page<TreatmentEntity> getTreatmentsByDoctor(Long doctorId, Pageable pageable) {

        userService.getUserById(doctorId);
        return treatmentRepository.findByTreatmentDoc_Id(doctorId, pageable);
    }


    public Page<TreatmentEntity> getTreatmentsByType(TreatmentType type, Pageable pageable) {
        return treatmentRepository.findByTreatmentType(type, pageable);
    }

    public Page<TreatmentEntity> getTreatmentsByStatus(TreatmentStatus status, Pageable pageable) {
        return treatmentRepository.findByTreatmentStatus(status, pageable);
    }

    public List<TreatmentEntity> getTreatmentsByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);
    }

    public List<TreatmentEntity> getTodayTreatments() {
        return treatmentRepository.findTodayTreatments(LocalDate.now());
    }

    public TreatmentEntity getTreatmentByCheckInId(Long checkInId) {
        List<TreatmentEntity> treatments = treatmentRepository.findByCheckInEntity_CheckInId(checkInId);
        if (treatments.isEmpty()) {
            throw new NotFoundException("해당 접수에 대한 진료를 찾을 수 없습니다. CheckInId: " + checkInId);
        }
        return treatments.get(0);
    }

    public Page<TreatmentEntity> getAllTreatments(Pageable pageable) {
        return treatmentRepository.findAll(pageable);
    }
    @Transactional
    public TreatmentEntity updateTreatment(Long treatmentId, TreatmentUpdateRequest request) {
        TreatmentEntity treatment = getTreatmentById(treatmentId);

        if (treatment.getTreatmentStatus() == TreatmentStatus.COMPLETED) {
            throw new BusinessException("완료된 진료는 수정할 수 없습니다.");
        }

        if (treatment.getTreatmentStatus() == TreatmentStatus.CANCELLED) {
            throw new BusinessException("취소된 진료는 수정할 수 없습니다.");
        }

        DepartmentEntity department = null;
        if (request.getDepartmentId() != null) {
            department = departmentService.getDepartmentById(request.getDepartmentId());
        }

        UserEntity doctor = null;
        if (request.getDoctorId() != null) {
            doctor = userService.getUserById(request.getDoctorId());
        }

        treatment.update(request.getComment(), department, doctor);

        return treatmentRepository.save(treatment);
    }

    @Transactional
    public void startTreatment(Long treatmentId) {
        TreatmentEntity treatment = getTreatmentById(treatmentId);

        if (treatment.getTreatmentStatus() == TreatmentStatus.IN_PROGRESS) {
            throw new BusinessException("이미 진행 중인 진료입니다.");
        }

        if (treatment.getTreatmentStatus() == TreatmentStatus.COMPLETED) {
            throw new BusinessException("완료된 진료는 시작할 수 없습니다.");
        }

        treatment.start();
        treatmentRepository.save(treatment);
    }

    @Transactional
    public void completeTreatment(Long treatmentId, TreatmentCompleteRequest request) {
        TreatmentEntity treatment = getTreatmentById(treatmentId);

        if (treatment.getTreatmentStatus() == TreatmentStatus.COMPLETED) {
            throw new BusinessException("이미 완료된 진료입니다.");
        }

        if (treatment.getTreatmentStatus() == TreatmentStatus.CANCELLED) {
            throw new BusinessException("취소된 진료는 완료할 수 없습니다.");
        }

        treatment.complete(request.getComment());
        treatmentRepository.save(treatment);

        if (autoCompleteReservationOnTreatmentComplete && treatment.getCheckInEntity() != null) {
            reservationService.completeReservationByCheckInId(treatment.getCheckInEntity().getCheckInId());
        }

        eventPublisher.publish(new TreatmentCompletedEvent(
                treatment.getTreatmentId(),
                resolvePatientNo(treatment),
                treatment.getTreatmentType().name()
        ));

        treatmentNotificationService.sendTreatmentCompletedNotification(treatment);
    }

    @Transactional
    public void cancelTreatment(Long treatmentId, String cancelReason) {
        TreatmentEntity treatment = getTreatmentById(treatmentId);

        if (treatment.getTreatmentStatus() == TreatmentStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 진료입니다.");
        }
        if (treatment.getTreatmentStatus() == TreatmentStatus.COMPLETED) {
            throw new BusinessException("완료된 진료는 취소할 수 없습니다.");
        }
        TreatmentPaymentGuard paymentGuard = treatmentPaymentGuard.getIfAvailable();
        if (paymentGuard != null && paymentGuard.hasCollectedPayment(treatmentId)) {
            throw new BusinessException("수납이 완료된 진료는 환불 후에만 취소할 수 있습니다.");
        }
        treatment.cancel(cancelReason != null ? cancelReason : "진료 취소");
        treatmentRepository.save(treatment);

        eventPublisher.publish(new TreatmentCancelledEvent(
                treatment.getTreatmentId(),
                resolvePatientNo(treatment),
                cancelReason
        ));
    }


    @Transactional
    public TreatmentEntity createTreatmentFromCheckIn(CheckInEntity checkIn) {

        if (checkIn.getCheckInStatus() != CheckInStatus.COMPLETED) {
            throw new BusinessException("완료된 접수만 진료를 생성할 수 있습니다.");
        }

        List<TreatmentEntity> existingTreatments = treatmentRepository.findByCheckInEntity_CheckInId(
                checkIn.getCheckInId());
        if (!existingTreatments.isEmpty()) {
            throw new DuplicateException("이미 해당 접수로 생성된 진료가 있습니다.");
        }

        DepartmentEntity department = checkIn.getDepartmentEntity();
        if (department == null) {
            throw new BusinessException("접수 진료과가 없습니다.");
        }

        UserEntity defaultDoctor = resolveDoctor(checkIn.getUserEntity().getId());

        TreatmentEntity treatment = TreatmentEntity.builder()
                .checkInEntity(checkIn)
                .patientEntity(checkIn.getPatientEntity())
                .treatmentType(TreatmentType.OUTPATIENT)
                .treatmentDate(LocalDateTime.now())
                .treatmentStatus(TreatmentStatus.PENDING)
                .departmentEntity(department)
                .treatmentDept(department.getName())
                .treatmentDoc(defaultDoctor)
                .build();

        TreatmentEntity saved;
        try {
            saved = treatmentRepository.saveAndFlush(treatment);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateException("이미 해당 접수로 생성된 진료가 있습니다.", exception);
        }

        createTreatmentTypeEntity(saved, TreatmentType.OUTPATIENT, checkIn);

        eventPublisher.publish(new TreatmentCreatedEvent(
                saved.getTreatmentId(),
                checkIn.getPatientEntity().getPatientNoValue(),
                checkIn.getPatientEntity().getPatientName(),
                saved.getTreatmentType().name(),
                saved.getTreatmentDate()
        ));

        return saved;
    }

    public Long findStoredPatientNo(Long treatmentId) {
        return treatmentRepository.findStoredPatientNo(treatmentId);
    }

    private Long resolvePatientNo(TreatmentEntity treatment) {
        if (treatment.getTreatmentId() != null) {
            return treatmentRepository.findStoredPatientNo(treatment.getTreatmentId());
        }
        return null;
    }

    private UserEntity resolveDoctor(Long userId) {
        UserEntity user = userService.getUserById(userId);
        if (user.getRole() == RoleType.DOCTOR) {
            return user;
        }
        return userService.getUsersByRole(RoleType.DOCTOR).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("진료 의사가 설정되지 않았습니다."));
    }
}