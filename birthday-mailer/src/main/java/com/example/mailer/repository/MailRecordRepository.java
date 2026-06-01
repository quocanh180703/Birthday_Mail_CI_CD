package com.example.mailer.repository;

import com.example.mailer.entity.MailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MailRecordRepository extends JpaRepository<MailRecord, Long> {
	@Query(value = "SELECT * FROM mail_record WHERE status = :status ORDER BY created_at DESC", nativeQuery = true)
	java.util.List<MailRecord> findRecentByStatusNative(String status, Pageable pageable);
}
