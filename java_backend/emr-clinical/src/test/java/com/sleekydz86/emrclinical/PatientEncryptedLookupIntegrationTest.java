package com.sleekydz86.emrclinical;

import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.security.encryption.BlindIndexService;
import com.sleekydz86.core.security.encryption.EncryptionService;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.common.valueobject.ResidentRegistrationNumber;
import com.sleekydz86.domain.patient.dto.PatientRegisterRequest;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.patient.service.PatientDuplicateChecker;
import com.sleekydz86.domain.patient.service.PatientService;
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
    private static final String SHARED_RRN_DIGITS = "9001011234567";
    private static final String SHARED_TEL = "01033334444";

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private BlindIndexService blindIndexService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientDuplicateChecker patientDuplicateChecker;

    @Autowired
    private PatientService patientService;

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
    @DisplayName("같은 주민번호는 하이픈 유무와 관계없이 조회되고 중복 등록은 거절된다")
    void sameRrnIsFoundRegardlessOfHyphenAndDuplicateRegisterIsRejected() {
        // given
        PatientEntity saved = savePatient("암호환자", SHARED_RRN, "p-enc-1@example.com", "010-3333-4444");
        entityManager.flush();
        entityManager.clear();

        // when
        List<PatientEntity> byHyphen = patientRepository.findByRrnHmac(blindIndexService.hmacRrn(SHARED_RRN));
        List<PatientEntity> byDigits = patientRepository.findByRrnHmac(blindIndexService.hmacRrn(SHARED_RRN_DIGITS));
        List<PatientEntity> byTel = patientRepository.findByTelHmac(blindIndexService.hmacPhone(SHARED_TEL));
        List<PatientEntity> duplicates = patientDuplicateChecker.checkDuplicate(
                SHARED_RRN_DIGITS, "p-enc-1@example.com", SHARED_TEL);
        Throwable thrown = catchThrowable(() -> patientService.registerPatient(
                PatientRegisterRequest.of(
                        "둘째환자",
                        SHARED_RRN,
                        "M",
                        LocalDate.of(1990, 1, 1),
                        "p-dup-2@example.com",
                        "010-5555-6666",
                        "N",
                        null)));

        // then
        assertThat(saved.getPatientRrnValue()).isEqualTo(SHARED_RRN);
        assertThat(saved.getPatientTelValue()).isEqualTo(SHARED_TEL);
        assertThat(saved.getRrnHmac()).isEqualTo(blindIndexService.hmacRrn(SHARED_RRN_DIGITS));
        assertThat(byHyphen).extracting(PatientEntity::getPatientNoValue).containsExactly(saved.getPatientNoValue());
        assertThat(byDigits).extracting(PatientEntity::getPatientNoValue).containsExactly(saved.getPatientNoValue());
        assertThat(byTel).extracting(PatientEntity::getPatientNoValue).containsExactly(saved.getPatientNoValue());
        assertThat(duplicates).extracting(PatientEntity::getPatientNoValue).containsExactly(saved.getPatientNoValue());
        assertThat(patientDuplicateChecker.checkDuplicateByRrn(SHARED_RRN_DIGITS))
                .extracting(PatientEntity::getPatientNoValue)
                .containsExactly(saved.getPatientNoValue());
        assertThat(thrown).isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이미 등록된 주민등록번호입니다.");
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
