package com.example.mailer.service;

import com.example.mailer.dto.MailRecordDto;
import com.example.mailer.dto.RecipientStatsDto;

import java.util.List;

//
// Contract cho business logic thống kê mail
// Tách biệt khỏi {@link IMailRecordService} để tuân thủ Single Responsibility Principle
//
public interface IMailStatisticsService {

    // Thống kê tổng hợp theo từng recipient:
    // tổng gửi, thành công, thất bại, tổng retry, trung bình retry khi thành công, lần gửi gần nhất

    List<RecipientStatsDto> getStatsByRecipient();

    /**
     * Lịch sử từng lần gửi của một recipient cụ thể, sắp xếp mới nhất trước.
     *
     * @param recipient địa chỉ email cần tra cứu
     */
    List<MailRecordDto> getHistoryByRecipient(String recipient);
}
