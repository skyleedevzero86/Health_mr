package com.sleekydz86.finance.payment.service;

import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentBatchService {

    private final PaymentRepository paymentRepository;
    private final PaymentNotificationService paymentNotificationService;


    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void processUnpaidPatients() {
        log.info("일일 미납 환자 배치 처리 시작");

        try {

            List<PaymentEntity> unpaidPayments = paymentRepository.findAll().stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.UNPAID ||
                            p.getPaymentStatus() == PaymentStatus.PARTIAL)
                    .filter(p -> {

                        if (p.getPaymentDate() != null) {
                            LocalDate paymentDate = p.getPaymentDate().toLocalDate();
                            return paymentDate.isBefore(LocalDate.now().minusDays(3));
                        }
                        return false;
                    })
                    .toList();

            log.info("미납 결제 건수: {}", unpaidPayments.size());


            for (PaymentEntity payment : unpaidPayments) {
                try {
                    paymentNotificationService.sendUnpaidNotification(payment);
                } catch (Exception e) {
                    log.error("미납 알림 발송 실패: PaymentId={}", payment.getPaymentId(), e);
                }
            }

            log.info("일일 미납 환자 배치 처리 완료: 처리 건수={}", unpaidPayments.size());
        } catch (Exception e) {
            log.error("일일 미납 환자 배치 처리 실패", e);
        }
    }


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(readOnly = true)
    public void generateDailyPaymentStatistics() {
        log.info("일일 결제 통계 생성 배치 처리 시작");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            List<PaymentEntity> yesterdayPayments = paymentRepository.findAll().stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            p.getPaymentDate().toLocalDate().equals(yesterday))
                    .toList();

            long totalCount = yesterdayPayments.size();
            long totalAmount = yesterdayPayments.stream()
                    .mapToLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                    .sum();

            log.info("일일 결제 통계 생성 완료: 날짜={}, 건수={}, 금액={}",
                    yesterday, totalCount, totalAmount);

        } catch (Exception e) {
            log.error("일일 결제 통계 생성 실패", e);
        }
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    @Transactional(readOnly = true)
    public void generateWeeklyPaymentStatistics() {
        log.info("주간 결제 통계 생성 배치 처리 시작");

        try {
            LocalDate weekStart = LocalDate.now().minusWeeks(1);
            LocalDate weekEnd = LocalDate.now().minusDays(1);

            List<PaymentEntity> weeklyPayments = paymentRepository.findAll().stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            !p.getPaymentDate().toLocalDate().isBefore(weekStart) &&
                            !p.getPaymentDate().toLocalDate().isAfter(weekEnd))
                    .toList();

            long totalCount = weeklyPayments.size();
            long totalAmount = weeklyPayments.stream()
                    .mapToLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                    .sum();

            log.info("주간 결제 통계 생성 완료: 기간={}~{}, 건수={}, 금액={}",
                    weekStart, weekEnd, totalCount, totalAmount);

        } catch (Exception e) {
            log.error("주간 결제 통계 생성 실패", e);
        }
    }


    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(readOnly = true)
    public void generateMonthlyPaymentStatistics() {
        log.info("월간 결제 통계 생성 배치 처리 시작");

        try {
            LocalDate monthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate monthEnd = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth());

            List<PaymentEntity> monthlyPayments = paymentRepository.findAll().stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            !p.getPaymentDate().toLocalDate().isBefore(monthStart) &&
                            !p.getPaymentDate().toLocalDate().isAfter(monthEnd))
                    .toList();

            long totalCount = monthlyPayments.size();
            long totalAmount = monthlyPayments.stream()
                    .mapToLong(p -> p.getPaymentCurrentMoney() != null ? p.getPaymentCurrentMoney() : 0L)
                    .sum();

            log.info("월간 결제 통계 생성 완료: 기간={}~{}, 건수={}, 금액={}",
                    monthStart, monthEnd, totalCount, totalAmount);

        } catch (Exception e) {
            log.error("월간 결제 통계 생성 실패", e);
        }
    }
}

