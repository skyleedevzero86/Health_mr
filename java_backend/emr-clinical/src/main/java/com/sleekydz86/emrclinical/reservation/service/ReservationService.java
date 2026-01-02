package com.sleekydz86.emrclinical.reservation.service;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.event.domain.ReservationCreatedEvent;
import com.sleekydz86.core.event.domain.ReservationUpdatedEvent;
import com.sleekydz86.core.event.domain.ReservationCancelledEvent;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.service.CheckInService;
import com.sleekydz86.emrclinical.reservation.dto.ReservationCreateRequest;
import com.sleekydz86.emrclinical.reservation.dto.ReservationUpdateRequest;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.reservation.notification.ReservationNotificationService;
import com.sleekydz86.emrclinical.reservation.repository.ReservationRepository;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService implements BaseService<ReservationEntity, Long> {

    private final ReservationRepository reservationRepository;
    private final PatientService patientService;
    private final UserService userService;
    private final EventPublisher eventPublisher;
    private final CheckInService checkInService;
    private final ReservationNotificationService reservationNotificationService;

    @Value("${clinical.integration.auto-create-checkin-on-reservation-complete:true}")
    private boolean autoCreateCheckInOnReservationComplete;

    @Transactional
    public ReservationEntity createReservation(ReservationCreateRequest request, Long userId) {

        PatientEntity patient = patientService.getPatientByNo(request.getPatientNo());

        UserEntity user = null;
        if (userId != null) {
            user = userService.getUserById(userId);
        }

        if (request.getReservationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("예약 날짜는 미래 날짜만 가능합니다.");
        }

        if (reservationRepository.existsByPatientEntity_PatientNoAndReservationDate(
                request.getPatientNo(), request.getReservationDate())) {
            throw new DuplicateException("이미 예약된 시간입니다.");
        }

        ReservationEntity reservation = ReservationEntity.builder()
                .patientEntity(patient)
                .userEntity(user)
                .reservationDate(request.getReservationDate())
                .reservationStatus(ReservationStatus.PENDING)
                .reservationYn("Y")
                .build();

        ReservationEntity saved = reservationRepository.save(reservation);

        eventPublisher.publish(new ReservationCreatedEvent(
                saved.getReservationId(),
                saved.getPatientEntity().getPatientNoValue(),
                saved.getPatientEntity().getPatientName(),
                saved.getReservationDate()));

        reservationNotificationService.sendReservationRegisteredNotification(saved);

        return saved;
    }

    public ReservationEntity getReservationById(Long reservationId) {
        return validateExists(reservationRepository, reservationId, "예약을 찾을 수 없습니다. ID: " + reservationId);
    }

    public Page<ReservationEntity> getReservationsByPatientNo(Long patientNo, Pageable pageable) {

        patientService.getPatientByNo(patientNo);
        return reservationRepository.findByPatientEntity_PatientNo(patientNo, pageable);
    }

    public List<ReservationEntity> getReservationsByDate(LocalDate date) {
        return reservationRepository.findTodayReservations(date);
    }

    public List<ReservationEntity> getReservationsByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return reservationRepository.findByReservationDateBetween(startDateTime, endDateTime);
    }

    public List<ReservationEntity> getTodayReservations() {
        return reservationRepository.findTodayReservations(LocalDate.now());
    }

    public Page<ReservationEntity> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        return reservationRepository.findByReservationStatus(status, pageable);
    }

    public List<ReservationEntity> getReservationsByDoctor(Long doctorId, LocalDate date) {

        userService.getUserById(doctorId);

        List<ReservationEntity> allReservations = reservationRepository.findByUserEntity_Id(doctorId);

        if (date != null) {
            return allReservations.stream()
                    .filter(r -> r.getReservationDate().toLocalDate().equals(date))
                    .toList();
        }

        return allReservations;
    }

    public Page<ReservationEntity> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Transactional
    public ReservationEntity updateReservation(Long reservationId, ReservationUpdateRequest request) {
        ReservationEntity reservation = getReservationById(reservationId);

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("취소된 예약은 수정할 수 없습니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException("완료된 예약은 수정할 수 없습니다.");
        }

        if (request.getNewReservationDate() != null) {
            if (request.getNewReservationDate().isBefore(LocalDateTime.now())) {
                throw new BusinessException("예약 날짜는 미래 날짜만 가능합니다.");
            }

            if (reservationRepository.existsByPatientEntity_PatientNoAndReservationDate(
                    reservation.getPatientEntity().getPatientNoValue(), request.getNewReservationDate())) {
                throw new DuplicateException("이미 예약된 시간입니다.");
            }
        }

        if (request.getReservationYn() != null && "N".equals(request.getReservationYn())) {
            reservation.cancel(
                    request.getReservationChangeCause() != null ? request.getReservationChangeCause() : "예약 취소");
        } else {

            reservation.update(request.getNewReservationDate(), request.getReservationChangeCause());
        }

        ReservationEntity saved = reservationRepository.save(reservation);

        if (request.getNewReservationDate() != null) {
            eventPublisher.publish(new ReservationUpdatedEvent(
                    saved.getReservationId(),
                    saved.getPatientEntity().getPatientNoValue(),
                    saved.getReservationDate()));

            reservationNotificationService.sendReservationUpdatedNotification(saved);
        }

        return saved;
    }

    @Transactional
    public void cancelReservation(Long reservationId, String cancelReason) {
        ReservationEntity reservation = getReservationById(reservationId);

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 예약입니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException("완료된 예약은 취소할 수 없습니다.");
        }

        reservation.cancel(cancelReason != null ? cancelReason : "예약 취소");
        reservationRepository.save(reservation);

        eventPublisher.publish(new ReservationCancelledEvent(
                reservation.getReservationId(),
                reservation.getPatientEntity().getPatientNoValue(),
                cancelReason));

        reservationNotificationService.sendReservationCancelledNotification(reservation, cancelReason);
    }

    @Transactional
    public void confirmReservation(Long reservationId) {
        ReservationEntity reservation = getReservationById(reservationId);

        if (reservation.getReservationStatus() == ReservationStatus.CONFIRMED) {
            throw new BusinessException("이미 확인된 예약입니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("취소된 예약은 확인할 수 없습니다.");
        }

        reservation.confirm();
        reservationRepository.save(reservation);
    }

    @Transactional
    public void completeReservation(Long reservationId) {
        ReservationEntity reservation = getReservationById(reservationId);

        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException("이미 완료된 예약입니다.");
        }

        reservation.complete();
        reservationRepository.save(reservation);

        if (autoCreateCheckInOnReservationComplete) {
            checkInService.createCheckInFromReservation(reservation);
        }
    }

    public Optional<ReservationEntity> findReservationByCheckInId(Long checkInId) {

        CheckInEntity checkIn = checkInService.getCheckInById(checkInId);

        // 환자 번호와 예약 날짜를 기준으로 예약 찾기
        // 예약 날짜가 접수 날짜와 같은 예약을 찾아야함.. 추후 확인
        List<ReservationEntity> reservations = reservationRepository.findByPatientEntity_PatientNo(
                checkIn.getPatientEntity().getPatientNoValue());

        return reservations.stream()
                .filter(r -> r.getReservationDate().toLocalDate().equals(checkIn.getCheckInDate().toLocalDate()))
                .filter(r -> r.getReservationStatus() != ReservationStatus.COMPLETED)
                .filter(r -> r.getReservationStatus() != ReservationStatus.CANCELLED)
                .findFirst();
    }

    @Transactional
    public void completeReservationByCheckInId(Long checkInId) {
        Optional<ReservationEntity> reservationOpt = findReservationByCheckInId(checkInId);
        if (reservationOpt.isPresent()) {
            ReservationEntity reservation = reservationOpt.get();
            if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
                reservation.complete();
                reservationRepository.save(reservation);
            }
        }
    }
}
