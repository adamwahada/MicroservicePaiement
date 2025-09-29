package Projet.Microservice.Services;

import Projet.Microservice.Entities.Payment;
import Projet.Microservice.Entities.PaymentStatus;
import Projet.Microservice.Repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentExpiryService {

    private final PaymentRepository paymentRepository;

    private static final long EXPIRY_MINUTES = 15;
    private static final long DELETION_DAYS = 2;

    // Run every minute: expire old payments
    @Scheduled(fixedRate = 60_000)
    public void expireUnfinishedPayments() {
        Instant expiryThreshold = Instant.now().minus(EXPIRY_MINUTES, ChronoUnit.MINUTES);

        List<Payment> paymentsToExpire = paymentRepository
                .findByPaymentStatusInAndCreatedAtBefore(
                        List.of(PaymentStatus.CREATED, PaymentStatus.PENDING),
                        expiryThreshold
                );

        for (Payment payment : paymentsToExpire) {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            payment.setFailureReason("Payment expired due to inactivity.");
            paymentRepository.save(payment);

            log.info("Payment {} expired due to inactivity.", payment.getPaymentId());
            // Optional: notify user
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteOldExpiredPayments() {
        Instant deletionThreshold = Instant.now().minus(DELETION_DAYS, ChronoUnit.DAYS);

        List<Payment> expiredPayments = paymentRepository
                .findByPaymentStatusAndCreatedAtBefore(PaymentStatus.EXPIRED, deletionThreshold);

        for (Payment payment : expiredPayments) {
            paymentRepository.delete(payment);
            log.info("Deleted expired payment {} for cleanup.", payment.getPaymentId());
        }
    }
}
