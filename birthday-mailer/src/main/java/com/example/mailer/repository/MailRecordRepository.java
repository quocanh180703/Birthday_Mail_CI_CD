package com.example.mailer.repository;

import com.example.mailer.entity.MailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailRecordRepository extends JpaRepository<MailRecord, Long> {
}
