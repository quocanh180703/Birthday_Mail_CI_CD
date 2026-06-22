package com.example.mailer.service;

import com.example.mailer.dto.MailRecordDto;
import com.example.mailer.entity.MailRecord;

import java.util.List;

//  Contract cho business logic query mail record
//  Chỉ xử lý truy vấn theo status — thống kê được tách sang {@link IMailStatisticsService}

public interface IMailRecordService {

    /**
     * Truy vấn danh sách MailRecord gần đây theo status.
     *
     * @param status trạng thái: PENDING / SENT / FAILED
     * @param limit  số lượng bản ghi tối đa
     */
    List<MailRecord> getRecentByStatus(String status, int limit);

    // Truy vấn danh sách MailRecord gần đây theo status, trả về DTO
    List<MailRecordDto> getRecentByStatusDto(String status, int limit);
}
