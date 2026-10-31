package com.sleekydz86.finance;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.lock.service.DistributedLockService;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.LoginId;
import com.sleekydz86.domain.common.valueobject.Password;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.common.valueobject.ResidentRegistrationNumber;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.department.type.DepartmentType;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.AccountStatus;
import com.sleekydz86.domain.user.type.Gender;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.service.CheckInService;
import com.sleekydz86.emrclinical.prescription.dto.PrescriptionCreateRequest;
import com.sleekydz86.emrclinical.prescription.dto.PrescriptionItemCreateRequest;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.prescription.service.PrescriptionService;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCompleteRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCreateRequest;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import com.sleekydz86.finance.payment.dto.PaymentRegisterRequest;
import com.sleekydz86.finance.payment.dto.PaymentResponse;
import com.sleekydz86.finance.payment.listener.TreatmentCompletedEventListener;
import com.sleekydz86.finance.payment.service.PaymentService;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EmrFinanceApplicationTests.TestApp.class)
@ActiveProfiles("test")
@Transactional
class VisitFlowIntegrationTest {

    private static final AtomicLong SEQ = new AtomicLong(8000001);

    @MockitoBean
    private DistributedLockService distributedLockService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private TreatmentCompletedEventListener treatmentCompletedEventListener;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("접수에서 수납까지 한 흐름으로 성공한다")
    void checkInThroughPaymentSucceeds() {
        stubInfrastructure();
        PatientEntity patient = savePatient("흐름환자");
        UserEntity doctor = saveDoctor("flowdoc");
        DepartmentEntity department = saveDepartment("FLW", "흐름과");

        CheckInEntity pending = checkInService.createCheckIn(patient.getPatientNoValue(), doctor.getId());
        assertThat(pending.getCheckInStatus()).isEqualTo(CheckInStatus.PENDING);
        CheckInEntity completed = checkInService.completeCheckIn(pending.getCheckInId());
        assertThat(completed.getCheckInStatus()).isEqualTo(CheckInStatus.COMPLETED);

        TreatmentCreateRequest treatmentRequest = new TreatmentCreateRequest();
        treatmentRequest.setTreatmentType(TreatmentType.OUTPATIENT);
        treatmentRequest.setDoctorId(doctor.getId());
        treatmentRequest.setCheckInId(completed.getCheckInId());
        treatmentRequest.setDepartmentId(department.getId());
        TreatmentEntity treatment = treatmentService.createTreatment(treatmentRequest);

        assertThat(treatment.getTreatmentId()).isNotNull();
        assertThat(treatment.getCheckInEntity().getCheckInId()).isEqualTo(completed.getCheckInId());
        Number storedPatientNo = (Number) entityManager.createNativeQuery(
                        "select patient_no from treatments where treatment_id = :id")
                .setParameter("id", treatment.getTreatmentId())
                .getSingleResult();
        assertThat(storedPatientNo.longValue()).isEqualTo(patient.getPatientNoValue());

        treatmentService.completeTreatment(treatment.getTreatmentId(), new TreatmentCompleteRequest());
        TreatmentEntity completedTreatment = treatmentService.getTreatmentById(treatment.getTreatmentId());
        assertThat(completedTreatment.getTreatmentStatus()).isEqualTo(TreatmentStatus.COMPLETED);

        PrescriptionCreateRequest prescriptionRequest = new PrescriptionCreateRequest();
        prescriptionRequest.setTreatmentId(treatment.getTreatmentId());
        prescriptionRequest.setPatientNo(patient.getPatientNoValue());
        prescriptionRequest.setPrescriptionDocId(doctor.getId());
        prescriptionRequest.setPrescriptionType(PrescriptionType.OUTPATIENT);
        prescriptionRequest.setPrescriptionItems(List.of(item()));
        PrescriptionEntity prescription = prescriptionService.registerPrescription(prescriptionRequest);
        assertThat(prescription.getPatientEntity().getPatientNoValue()).isEqualTo(patient.getPatientNoValue());

        PaymentRegisterRequest paymentRequest = new PaymentRegisterRequest();
        paymentRequest.setTreatmentId(treatment.getTreatmentId());
        paymentRequest.setPaymentMethod(PaymentMethod.CASH);
        PaymentResponse payment = paymentService.registerPayment(paymentRequest);

        assertThat(payment.getPatientNo()).isEqualTo(patient.getPatientNoValue());
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(payment.getTreatmentId()).isEqualTo(treatment.getTreatmentId());
    }

    @Test
    @DisplayName("완료되지 않은 접수로 진료를 만들면 거절된다")
    void treatmentWithoutCompletedCheckInIsRejected() {
        stubInfrastructure();
        PatientEntity patient = savePatient("거절환자");
        UserEntity doctor = saveDoctor("rejdoc");

        CheckInEntity pending = checkInService.createCheckIn(patient.getPatientNoValue(), doctor.getId());
        TreatmentCreateRequest request = new TreatmentCreateRequest();
        request.setTreatmentType(TreatmentType.OUTPATIENT);
        request.setDoctorId(doctor.getId());
        request.setCheckInId(pending.getCheckInId());

        assertThatThrownBy(() -> treatmentService.createTreatment(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("완료된 접수만 진료를 생성할 수 있습니다.");
    }

    @Test
    @DisplayName("응급 진료도 접수 없이 만들 수 없다")
    void emergencyTreatmentRequiresCheckIn() {
        stubInfrastructure();
        UserEntity doctor = saveDoctor("erdoc");
        TreatmentCreateRequest request = new TreatmentCreateRequest();
        request.setTreatmentType(TreatmentType.EMERGENCY);
        request.setDoctorId(doctor.getId());

        assertThatThrownBy(() -> treatmentService.createTreatment(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("접수는 필수입니다.");
    }

    private void stubInfrastructure() {
        when(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).thenReturn("lock");
        when(distributedLockService.unlock(anyString(), anyString())).thenReturn(true);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> ops = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get(any())).thenReturn(null);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> stringOps = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringOps);
        when(stringOps.get(any())).thenReturn(null);
        when(stringOps.setIfAbsent(any(), any(), anyLong(), any())).thenReturn(true);
    }

    private PatientEntity savePatient(String name) {
        long seq = SEQ.getAndIncrement();
        PatientEntity patient = PatientEntity.builder()
                .patientNo(PatientNumber.of(seq))
                .patientName(name)
                .patientRrn(ResidentRegistrationNumber.of("900101-" + String.format("%07d", seq)))
                .patientGender("M")
                .patientBirth(LocalDate.of(1990, 1, 1))
                .patientEmail(Email.of("p" + seq + "@example.com"))
                .patientTel(PhoneNumber.of("010-" + String.format("%04d", seq % 10000) + "-1000"))
                .isForeign(false)
                .build();
        return patientRepository.save(patient);
    }

    private UserEntity saveDoctor(String loginId) {
        UserEntity doctor = UserEntity.builder()
                .role(RoleType.DOCTOR)
                .accountStatus(AccountStatus.ACTIVE)
                .loginId(LoginId.of(loginId))
                .password(Password.fromEncrypted("encoded-password"))
                .name("진료의")
                .gender(Gender.MALE)
                .email(Email.of(loginId + "@example.com"))
                .telNum(PhoneNumber.of("010-2222-" + String.format("%04d", SEQ.getAndIncrement() % 10000)))
                .build();
        return userRepository.save(doctor);
    }

    private DepartmentEntity saveDepartment(String code, String name) {
        return departmentRepository.save(DepartmentEntity.builder()
                .code(code)
                .name(name)
                .type(DepartmentType.CLINICAL)
                .build());
    }

    private PrescriptionItemCreateRequest item() {
        PrescriptionItemCreateRequest item = new PrescriptionItemCreateRequest();
        item.setDrugCode("TEST001");
        item.setDrugName("시험약");
        item.setDosage("1일 3회 식후");
        item.setDose("1정");
        item.setFrequency(3);
        item.setDays(3);
        item.setTotalQuantity(9);
        item.setUnit("정");
        return item;
    }
}
