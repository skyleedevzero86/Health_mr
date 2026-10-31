package com.sleekydz86.emrclinical;

import com.sleekydz86.core.security.encryption.EncryptionService;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.common.valueobject.ResidentRegistrationNumber;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.patient.service.PatientDuplicateChecker;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest(classes = EmrClinicalApplicationTests.TestApp.class)
@ActiveProfiles("test")
@Transactional
class PatientEncryptedLookupIntegrationTest {

    private static final AtomicLong SEQ = new AtomicLong(9100001);
    private static final String SHARED_RRN = "900101-1234567";
    private static final String SHARED_TEL = "01033334444";

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientDuplicateChecker patientDuplicateChecker;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("같은 주민번호도 AES-GCM 암호화 결과가 매번 달라진다")
    void sameRrnProducesDifferentCiphertext() {
        // given
        String rrn = SHARED_RRN;

        // when
        String first = encryptionService.encrypt(rrn);
        String second = encryptionService.encrypt(rrn);

        // then
        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
        assertThat(encryptionService.decrypt(first)).isEqualTo(rrn);
        assertThat(encryptionService.decrypt(second)).isEqualTo(rrn);
    }

    @Test
    @DisplayName("주민번호와 전화번호 평문 조회·중복 검사는 저장된 환자를 찾지 못한다")
    void plaintextRrnAndTelLookupDoesNotFindSavedPatient() {
        // given
        PatientEntity saved = savePatient("암호환자", SHARED_RRN, "p-enc-1@example.com", "010-3333-4444");
        entityManager.flush();

        // when
        boolean rrnExists = patientRepository.existsByPatientRrn(SHARED_RRN);
        var byRrn = patientRepository.findByPatientRrn(SHARED_RRN);
        var byTel = patientRepository.findByPatientTel(SHARED_TEL);
        List<PatientEntity> duplicates = patientDuplicateChecker.checkDuplicate(
                SHARED_RRN, "p-enc-1@example.com", SHARED_TEL);

        // then
        assertThat(saved.getPatientRrnValue()).isEqualTo(SHARED_RRN);
        assertThat(saved.getPatientTelValue()).isEqualTo(SHARED_TEL);
        assertThat(rrnExists).isFalse();
        assertThat(byRrn).isEmpty();
        assertThat(byTel).isEmpty();
        assertThat(duplicates).isEmpty();
        assertThat(patientDuplicateChecker.checkDuplicateByRrn(SHARED_RRN)).isEmpty();
        assertThat(patientDuplicateChecker.checkDuplicateByTel(SHARED_TEL)).isEmpty();
    }

    @Test
    @DisplayName("같은 주민번호로 두 환자를 저장해도 unique 제약이 막지 못한다")
    void uniqueConstraintDoesNotBlockDuplicateRrn() {
        // given
        PatientEntity first = savePatient("첫째환자", SHARED_RRN, "p-dup-1@example.com", "010-1111-2222");
        entityManager.flush();
        PatientEntity[] second = new PatientEntity[1];

        // when
        Throwable thrown = catchThrowable(() -> {
            second[0] = savePatient("둘째환자", SHARED_RRN, "p-dup-2@example.com", "010-5555-6666");
            entityManager.flush();
        });
        List<?> ciphertexts = second[0] == null
                ? List.of()
                : entityManager.createNativeQuery(
                        "select patient_rrn from patient where patient_no in (:firstNo, :secondNo)")
                .setParameter("firstNo", first.getPatientNoValue())
                .setParameter("secondNo", second[0].getPatientNoValue())
                .getResultList();

        // then
        assertThat(thrown).isNull();
        assertThat(patientRepository.findByPatientRrn(SHARED_RRN)).isEmpty();
        assertThat(ciphertexts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(String.valueOf(ciphertexts.get(0))).isNotEqualTo(String.valueOf(ciphertexts.get(1)));
    }

    private PatientEntity savePatient(String name, String rrn, String email, String tel) {
        long seq = SEQ.getAndIncrement();
        PatientEntity patient = PatientEntity.builder()
                .patientNo(PatientNumber.of(seq))
                .patientName(name)
                .patientRrn(ResidentRegistrationNumber.of(rrn))
                .patientGender("M")
                .patientBirth(LocalDate.of(1990, 1, 1))
                .patientEmail(Email.of(email))
                .patientTel(PhoneNumber.of(tel))
                .isForeign(false)
                .build();
        return patientRepository.save(patient);
    }
}
