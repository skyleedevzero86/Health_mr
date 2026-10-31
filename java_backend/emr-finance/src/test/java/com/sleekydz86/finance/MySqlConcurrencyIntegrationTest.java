package com.sleekydz86.finance;

import com.sleekydz86.core.audit.entity.AuditEntity;
import com.sleekydz86.core.audit.repository.AuditRepository;
import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.lock.service.DistributedLockService;
import com.sleekydz86.core.lock.service.IdempotencyService;
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
import com.sleekydz86.emrclinical.prescription.repository.PrescriptionRepository;
import com.sleekydz86.emrclinical.prescription.service.PrescriptionService;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCompleteRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCreateRequest;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.payment.controller.PaymentController;
import com.sleekydz86.finance.payment.dto.PaymentCancelRequest;
import com.sleekydz86.finance.payment.dto.PaymentCompleteRequest;
import com.sleekydz86.finance.payment.dto.PaymentRefundRequest;
import com.sleekydz86.finance.payment.dto.PaymentResponse;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.listener.TreatmentCompletedEventListener;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.payment.service.PaymentService;
import com.sleekydz86.finance.type.PaymentMethod;
import com.sleekydz86.finance.type.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EmrFinanceApplicationTests.TestApp.class)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MySqlConcurrencyIntegrationTest {

    private static final AtomicLong SEQUENCE = new AtomicLong(9100001);

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("health_mr_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.mysql.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.mysql.jdbc-url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.mysql.username", MYSQL::getUsername);
        registry.add("spring.datasource.mysql.password", MYSQL::getPassword);
        registry.add("spring.datasource.mysql.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @MockitoBean
    private DistributedLockService distributedLockService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

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
    private TreatmentRepository treatmentRepository;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void simulateUnavailableRedis() {
        when(distributedLockService.tryLock(anyString(), anyLong(), anyLong()))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));
        when(idempotencyService.getPreviousResult(anyString(), any()))
                .thenReturn(null);
        when(idempotencyService.checkAndSet(anyString(), anyLong()))
                .thenReturn(false);
        doThrow(new DataAccessResourceFailureException("redis unavailable"))
                .when(idempotencyService).saveResult(anyString(), any(), anyLong());
        when(redisTemplate.opsForValue())
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("concurrent payment completion changes state once and retry is already processed")
    void concurrentPaymentCompletionIsAppliedOnce() throws Exception {
        Fixture fixture = createFixture();
        PaymentEntity payment = savePayment(fixture.treatment(), fixture.patient(), PaymentStatus.UNPAID, 0L, 10000L);
        PaymentCompleteRequest request = completeRequest();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        List<PaymentResponse> responses;
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<PaymentResponse> first = executor.submit(() -> completeTogether(payment.getPaymentId(), request, ready, start));
            Future<PaymentResponse> second = executor.submit(() -> completeTogether(payment.getPaymentId(), request, ready, start));
            ready.await();
            start.countDown();
            responses = List.of(first.get(), second.get());
        }

        assertThat(responses).allMatch(response -> response.getPaymentStatus() == PaymentStatus.PAID);
        assertThat(responses.stream().filter(PaymentResponse::isAlreadyProcessed)).hasSize(1);
        assertThat(responses.stream().filter(response -> !response.isAlreadyProcessed())).hasSize(1);
        PaymentResponse retry = paymentService.completePayment(payment.getPaymentId(), request);
        assertThat(retry.isAlreadyProcessed()).isTrue();
        ResponseEntity<Map<String, Object>> apiRetry = paymentController.completePayment(payment.getPaymentId(), request);
        assertThat(apiRetry.getStatusCode().value()).isEqualTo(200);
        assertThat(((PaymentResponse) apiRetry.getBody().get("data")).isAlreadyProcessed()).isTrue();
    }

    @Test
    @DisplayName("concurrent treatment creation for one check-in stores one treatment")
    void concurrentTreatmentCreationStoresOneTreatment() throws Exception {
        Fixture fixture = createFixtureWithoutTreatment();
        TreatmentCreateRequest request = treatmentRequest(fixture.checkIn(), fixture.doctor(), fixture.department());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        int success = 0;
        int duplicate = 0;

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<TreatmentEntity> first = executor.submit(() -> createTreatmentTogether(request, ready, start));
            Future<TreatmentEntity> second = executor.submit(() -> createTreatmentTogether(request, ready, start));
            ready.await();
            start.countDown();
            for (Future<TreatmentEntity> future : List.of(first, second)) {
                try {
                    future.get();
                    success++;
                } catch (ExecutionException exception) {
                    assertThat(exception.getCause()).isInstanceOf(DuplicateException.class);
                    duplicate++;
                }
            }
        }

        assertThat(success).isEqualTo(1);
        assertThat(duplicate).isEqualTo(1);
        assertThat(treatmentRepository.findByCheckInEntity_CheckInId(fixture.checkIn().getCheckInId())).hasSize(1);
    }

    @Test
    @DisplayName("duplicate check-in is rejected and cancelled check-in frees the slot")
    void duplicateCheckInRuleUsesActiveSlot() {
        PatientEntity patient = savePatient();
        DepartmentEntity department = saveDepartment();
        UserEntity doctor = saveDoctor(department);
        CheckInEntity first = checkInService.createCheckIn(patient.getPatientNoValue(), doctor.getId(), department.getId());

        assertThatThrownBy(() -> checkInService.createCheckIn(
                patient.getPatientNoValue(), doctor.getId(), department.getId()))
                .isInstanceOf(DuplicateException.class);

        checkInService.cancelCheckIn(first.getCheckInId());
        CheckInEntity replacement = checkInService.createCheckIn(
                patient.getPatientNoValue(), doctor.getId(), department.getId());
        assertThat(replacement.getCheckInStatus()).isEqualTo(CheckInStatus.PENDING);
    }

    @Test
    @DisplayName("cancellation guards reject invalid states and full refund succeeds")
    void cancellationRulesAndFullRefund() {
        Fixture completedFixture = createFixture();
        assertThatThrownBy(() -> checkInService.cancelCheckIn(completedFixture.checkIn().getCheckInId()))
                .isInstanceOf(BusinessException.class);

        treatmentService.completeTreatment(completedFixture.treatment().getTreatmentId(), new TreatmentCompleteRequest());
        assertThatThrownBy(() -> treatmentService.cancelTreatment(completedFixture.treatment().getTreatmentId(), "cancel"))
                .isInstanceOf(BusinessException.class);

        Fixture paidFixture = createFixture();
        PaymentEntity paid = savePayment(paidFixture.treatment(), paidFixture.patient(), PaymentStatus.PAID, 10000L, 0L);
        assertThatThrownBy(() -> treatmentService.cancelTreatment(paidFixture.treatment().getTreatmentId(), "cancel"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> paymentService.cancelPayment(paid.getPaymentId(), new PaymentCancelRequest("cancel")))
                .isInstanceOf(BusinessException.class);

        Fixture unpaidFixture = createFixture();
        PaymentEntity unpaid = savePayment(unpaidFixture.treatment(), unpaidFixture.patient(), PaymentStatus.UNPAID, 0L, 10000L);
        treatmentService.cancelTreatment(unpaidFixture.treatment().getTreatmentId(), "cancel");
        assertThat(treatmentService.getTreatmentById(unpaidFixture.treatment().getTreatmentId()).getTreatmentStatus())
                .isEqualTo(TreatmentStatus.CANCELLED);
        assertThatThrownBy(() -> paymentService.refundPayment(
                unpaid.getPaymentId(), new PaymentRefundRequest(10000L, PaymentMethod.CASH)))
                .isInstanceOf(BusinessException.class);

        PaymentResponse refunded = paymentService.refundPayment(
                paid.getPaymentId(), new PaymentRefundRequest(10000L, PaymentMethod.CASH));
        assertThat(refunded.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refunded.getPaymentCurrentMoney()).isZero();

        Fixture prescriptionFixture = createFixture();
        PrescriptionEntity prescription = saveDispensedPrescription(prescriptionFixture);
        assertThatThrownBy(() -> prescriptionService.cancelPrescription(prescription.getPrescriptionId(), "cancel"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("rolled back request writes fail audit only and personal fields are removed")
    void rollbackAuditIsFailOnlyAndPersonalDataIsRemoved() {
        Fixture fixture = createFixture();
        PaymentEntity paid = savePayment(fixture.treatment(), fixture.patient(), PaymentStatus.PAID, 10000L, 0L);
        authenticate(fixture.doctor().getId());

        assertThatThrownBy(() -> paymentService.cancelPayment(paid.getPaymentId(), new PaymentCancelRequest("cancel")))
                .isInstanceOf(BusinessException.class);

        List<AuditEntity> requestAudits = auditRepository.findAll().stream()
                .filter(audit -> audit.getUserId().equals(fixture.doctor().getId()))
                .filter(audit -> "PaymentService".equals(audit.getEntityType()))
                .filter(audit -> "DELETE".equals(audit.getActionType()))
                .toList();
        assertThat(requestAudits).extracting(AuditEntity::getOutcome).contains("FAIL").doesNotContain("SUCCESS");

        auditService.logAudit(
                fixture.doctor().getId(),
                "UPDATE",
                "Patient",
                fixture.patient().getPatientNoValue().toString(),
                null,
                Map.of(
                        "patientName", "sensitive-name",
                        "patientRrn", "900101-1234567",
                        "patientTel", "010-0000-0000",
                        "safeField", "kept"),
                null,
                null,
                "SUCCESS");
        AuditEntity sanitized = auditRepository.findAll().stream()
                .filter(audit -> "Patient".equals(audit.getEntityType()))
                .reduce((first, second) -> second)
                .orElseThrow();
        assertThat(sanitized.getAfterData())
                .contains("safeField")
                .doesNotContain("sensitive-name", "900101-1234567", "010-0000-0000");
    }

    private PaymentResponse completeTogether(
            Long paymentId,
            PaymentCompleteRequest request,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        return paymentService.completePayment(paymentId, request);
    }

    private TreatmentEntity createTreatmentTogether(
            TreatmentCreateRequest request,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        return treatmentService.createTreatment(request);
    }

    private Fixture createFixture() {
        Fixture fixture = createFixtureWithoutTreatment();
        TreatmentEntity treatment = treatmentService.createTreatment(
                treatmentRequest(fixture.checkIn(), fixture.doctor(), fixture.department()));
        return new Fixture(fixture.patient(), fixture.doctor(), fixture.department(), fixture.checkIn(), treatment);
    }

    private Fixture createFixtureWithoutTreatment() {
        PatientEntity patient = savePatient();
        DepartmentEntity department = saveDepartment();
        UserEntity doctor = saveDoctor(department);
        CheckInEntity checkIn = checkInService.createCheckIn(
                patient.getPatientNoValue(), doctor.getId(), department.getId());
        CheckInEntity completed = checkInService.completeCheckIn(checkIn.getCheckInId());
        return new Fixture(patient, doctor, department, completed, null);
    }

    private TreatmentCreateRequest treatmentRequest(
            CheckInEntity checkIn,
            UserEntity doctor,
            DepartmentEntity department) {
        TreatmentCreateRequest request = new TreatmentCreateRequest();
        request.setTreatmentType(TreatmentType.OUTPATIENT);
        request.setDoctorId(doctor.getId());
        request.setCheckInId(checkIn.getCheckInId());
        request.setPatientNo(checkIn.getPatientEntity().getPatientNoValue());
        request.setDepartmentId(department.getId());
        return request;
    }

    private PaymentCompleteRequest completeRequest() {
        PaymentCompleteRequest request = new PaymentCompleteRequest();
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setApprovalNumber("approval");
        request.setCardCompany("test-card");
        return request;
    }

    private PaymentEntity savePayment(
            TreatmentEntity treatment,
            PatientEntity patient,
            PaymentStatus status,
            long current,
            long remain) {
        return paymentRepository.saveAndFlush(PaymentEntity.builder()
                .treatmentEntity(treatment)
                .patientEntity(patient)
                .paymentStatus(status)
                .paymentTotalAmount(Money.of(10000L))
                .paymentSelfPay(Money.of(10000L))
                .paymentInsuranceMoney(Money.zero())
                .paymentCurrentMoney(Money.of(current))
                .paymentAmount(Money.zero())
                .paymentRemainMoney(Money.of(remain))
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDateTime.now())
                .build());
    }

    private PrescriptionEntity saveDispensedPrescription(Fixture fixture) {
        PrescriptionItemCreateRequest item = new PrescriptionItemCreateRequest();
        item.setDrugCode("TEST001");
        item.setDrugName("test-drug");
        item.setDosage("after meal");
        item.setDose("one");
        item.setFrequency(1);
        item.setDays(1);
        item.setTotalQuantity(1);
        item.setUnit("tablet");
        PrescriptionCreateRequest request = new PrescriptionCreateRequest();
        request.setTreatmentId(fixture.treatment().getTreatmentId());
        request.setPatientNo(fixture.patient().getPatientNoValue());
        request.setPrescriptionDocId(fixture.doctor().getId());
        request.setPrescriptionType(PrescriptionType.OUTPATIENT);
        request.setPrescriptionItems(List.of(item));
        PrescriptionEntity prescription = prescriptionService.registerPrescription(request);
        prescription.prescribe();
        prescriptionRepository.saveAndFlush(prescription);
        prescriptionService.dispensePrescription(prescription.getPrescriptionId());
        return prescriptionService.getPrescriptionById(prescription.getPrescriptionId());
    }

    private PatientEntity savePatient() {
        long sequence = SEQUENCE.getAndIncrement();
        return patientRepository.saveAndFlush(PatientEntity.builder()
                .patientNo(PatientNumber.of(sequence))
                .patientName("patient-" + sequence)
                .patientRrn(ResidentRegistrationNumber.of("900101-" + sequence))
                .patientGender("M")
                .patientBirth(LocalDate.of(1990, 1, 1))
                .patientEmail(Email.of("patient" + sequence + "@example.com"))
                .patientTel(PhoneNumber.of("010-" + String.format("%04d", sequence % 10000) + "-1000"))
                .isForeign(false)
                .build());
    }

    private DepartmentEntity saveDepartment() {
        long sequence = SEQUENCE.getAndIncrement();
        return departmentRepository.saveAndFlush(DepartmentEntity.builder()
                .code("D" + sequence)
                .name("department-" + sequence)
                .type(DepartmentType.CLINICAL)
                .build());
    }

    private UserEntity saveDoctor(DepartmentEntity department) {
        long sequence = SEQUENCE.getAndIncrement();
        return userRepository.saveAndFlush(UserEntity.builder()
                .role(RoleType.DOCTOR)
                .accountStatus(AccountStatus.ACTIVE)
                .loginId(LoginId.of("doctor" + sequence))
                .password(Password.fromEncrypted("encoded-password"))
                .name("doctor-" + sequence)
                .gender(Gender.MALE)
                .email(Email.of("doctor" + sequence + "@example.com"))
                .telNum(PhoneNumber.of("010-2000-" + String.format("%04d", sequence % 10000)))
                .department(department)
                .build());
    }

    private void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private record Fixture(
            PatientEntity patient,
            UserEntity doctor,
            DepartmentEntity department,
            CheckInEntity checkIn,
            TreatmentEntity treatment) {
    }
}
