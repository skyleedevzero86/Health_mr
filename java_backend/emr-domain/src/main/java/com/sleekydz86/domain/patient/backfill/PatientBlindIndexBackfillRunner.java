package com.sleekydz86.domain.patient.backfill;

import com.sleekydz86.core.security.encryption.BlindIndexService;
import com.sleekydz86.core.security.encryption.EncryptionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "patient.blind-index.backfill-enabled", havingValue = "true")
public class PatientBlindIndexBackfillRunner implements ApplicationRunner {

    private final EntityManager entityManager;
    private final EncryptionService encryptionService;
    private final BlindIndexService blindIndexService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String tableName = resolveTableName();
            if (tableName == null) {
                return;
            }
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            Integer updated = transactionTemplate.execute(status -> {
                int count = backfillRows(tableName);
                dropLegacyUniqueIndexes(tableName);
                return count;
            });

            Map<String, List<Long>> duplicateRrns = transactionTemplate.execute(status ->
                    findDuplicatePatientNos(tableName, "rrn_hmac"));
            if (duplicateRrns != null && !duplicateRrns.isEmpty()) {
                log.warn("주민번호 블라인드 인덱스 중복 그룹 수={}", duplicateRrns.size());
                for (List<Long> patientNos : duplicateRrns.values()) {
                    log.warn("주민번호 블라인드 인덱스 중복 환자번호={}", patientNos);
                }
                log.warn("중복 주민번호가 있어 unique를 적용하지 않았습니다.");
            } else if (createUniqueIndex(tableName, "uk_patient_rrn_hmac", "rrn_hmac")) {
                log.info("주민번호 블라인드 인덱스 unique를 적용했습니다. 대상 건수={}", updated);
            }

            Map<String, List<Long>> duplicatePassports = transactionTemplate.execute(status ->
                    findDuplicatePatientNos(tableName, "passport_hmac"));
            if (duplicatePassports != null && !duplicatePassports.isEmpty()) {
                log.warn("여권번호 블라인드 인덱스 중복 그룹 수={}", duplicatePassports.size());
                for (List<Long> patientNos : duplicatePassports.values()) {
                    log.warn("여권번호 블라인드 인덱스 중복 환자번호={}", patientNos);
                }
                log.warn("중복 여권번호가 있어 unique를 적용하지 않았습니다.");
            } else {
                createUniqueIndex(tableName, "uk_patient_passport_hmac", "passport_hmac");
            }
        } catch (RuntimeException exception) {
            log.warn("블라인드 인덱스 백필에 실패했습니다", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private int backfillRows(String tableName) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "select `patient_no`, `patient_rrn`, `patient_tel`, `patient_email`, `patient_passport` from `"
                        + tableName + "`")
                .getResultList();
        int updated = 0;
        for (Object[] row : rows) {
            Long patientNo = toLong(row[0]);
            if (patientNo == null) {
                continue;
            }
            entityManager.createNativeQuery(
                    "update `" + tableName + "` set `rrn_hmac` = :rrnHmac, `tel_hmac` = :telHmac, "
                            + "`email_hmac` = :emailHmac, `passport_hmac` = :passportHmac "
                            + "where `patient_no` = :patientNo")
                    .setParameter("rrnHmac", blindIndexService.hmacRrn(decryptOrPlain(row[1])))
                    .setParameter("telHmac", blindIndexService.hmacPhone(decryptOrPlain(row[2])))
                    .setParameter("emailHmac", blindIndexService.hmacEmail(decryptOrPlain(row[3])))
                    .setParameter("passportHmac", blindIndexService.hmacPassport(decryptOrPlain(row[4])))
                    .setParameter("patientNo", patientNo)
                    .executeUpdate();
            updated += 1;
        }
        return updated;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Long>> findDuplicatePatientNos(String tableName, String columnName) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "select p.`" + columnName + "`, p.`patient_no` from `" + tableName + "` p "
                        + "where p.`" + columnName + "` is not null "
                        + "and p.`" + columnName + "` in ("
                        + "select `" + columnName + "` from `" + tableName + "` "
                        + "where `" + columnName + "` is not null "
                        + "group by `" + columnName + "` having count(*) > 1)")
                .getResultList();
        Map<String, List<Long>> grouped = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String hmac = row[0] == null ? null : String.valueOf(row[0]);
            Long patientNo = toLong(row[1]);
            if (hmac == null || patientNo == null) {
                continue;
            }
            grouped.computeIfAbsent(hmac, key -> new ArrayList<>()).add(patientNo);
        }
        return grouped;
    }

    @SuppressWarnings("unchecked")
    private void dropLegacyUniqueIndexes(String tableName) {
        List<String> indexNames = entityManager.createNativeQuery(
                "select distinct index_name from information_schema.statistics "
                        + "where table_schema = database() "
                        + "and lower(table_name) = lower(:tableName) "
                        + "and column_name in ('patient_rrn', 'patient_email', 'patient_tel', 'patient_passport') "
                        + "and non_unique = 0")
                .setParameter("tableName", tableName)
                .getResultList();
        for (String indexName : indexNames) {
            if (indexName == null || "PRIMARY".equalsIgnoreCase(indexName)) {
                continue;
            }
            try {
                entityManager.createNativeQuery(
                        "alter table `" + tableName + "` drop index `" + indexName + "`")
                        .executeUpdate();
            } catch (RuntimeException exception) {
                log.warn("기존 unique 인덱스 제거에 실패했습니다. index={}", indexName, exception);
            }
        }
    }

    private boolean createUniqueIndex(String tableName, String indexName, String columnName) {
        if (indexExists(tableName, indexName)) {
            return true;
        }
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            transactionTemplate.executeWithoutResult(status -> entityManager.createNativeQuery(
                    "create unique index `" + indexName + "` on `" + tableName + "` (`" + columnName + "`)")
                    .executeUpdate());
            return true;
        } catch (RuntimeException exception) {
            log.warn("블라인드 인덱스 unique 적용에 실패했습니다. index={}", indexName, exception);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean indexExists(String tableName, String indexName) {
        List<?> rows = entityManager.createNativeQuery(
                "select 1 from information_schema.statistics "
                        + "where table_schema = database() "
                        + "and lower(table_name) = lower(:tableName) "
                        + "and index_name = :indexName")
                .setParameter("tableName", tableName)
                .setParameter("indexName", indexName)
                .setMaxResults(1)
                .getResultList();
        return !rows.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private String resolveTableName() {
        List<String> names = entityManager.createNativeQuery(
                "select table_name from information_schema.tables "
                        + "where table_schema = database() "
                        + "and lower(table_name) in ('patient', 'patient_entity')")
                .getResultList();
        if (names.isEmpty()) {
            log.warn("환자 테이블을 찾지 못해 블라인드 인덱스 백필을 건너뜁니다.");
            return null;
        }
        return names.get(0);
    }

    private String decryptOrPlain(Object value) {
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value);
        if (raw.isBlank() || !looksLikeBase64(raw)) {
            return raw;
        }
        try {
            return encryptionService.decrypt(raw);
        } catch (RuntimeException exception) {
            return raw;
        }
    }

    private boolean looksLikeBase64(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
