package com.example.mailer.repository;

import com.example.mailer.entity.MailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRecordRepository extends JpaRepository<MailRecord, Long> {

    @Query(value = "SELECT * FROM mail_record WHERE status = :status ORDER BY created_at DESC", nativeQuery = true)
    List<MailRecord> findRecentByStatusNative(@Param("status") String status, Pageable pageable);

    /**
     * Thống kê theo từng recipient:
     * - Tổng số lần gửi
     * - Số lần thành công (SENT)
     * - Số lần thất bại (FAILED)
     * - Tổng số lần thử retry (tính cả thất bại)
     * - Trung bình số lần thử mới thành công (chỉ tính bản ghi SENT)
     * - Lần gửi gần nhất
     */
    @Query(value = """
            SELECT
                m.recipient                                          AS recipient,
                m.sent_by                                           AS sentBy,
                COUNT(*)                                            AS totalSent,
                SUM(CASE WHEN m.status = 'SENT'   THEN 1 ELSE 0 END) AS successCount,
                SUM(CASE WHEN m.status = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
                SUM(m.attempts)                                     AS totalAttempts,
                AVG(CASE WHEN m.status = 'SENT' THEN m.attempts ELSE NULL END) AS avgAttemptsOnSuccess,
                MAX(m.created_at)                                   AS lastSentAt
            FROM mail_record m
            GROUP BY m.recipient, m.sent_by
            ORDER BY m.recipient
            """, nativeQuery = true)
    List<Object[]> findSendingStatsByRecipient();

    /**
     * Thống kê cho một recipient cụ thể — lịch sử từng lần gửi
     */
    @Query(value = """
            SELECT * FROM mail_record
            WHERE recipient = :recipient
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<MailRecord> findAllByRecipient(@Param("recipient") String recipient);
}
