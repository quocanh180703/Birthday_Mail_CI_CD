package com.example.mailer.service;

import com.example.mailer.model.Employee;

public interface IMailService {

    /**
     * Gửi email sinh nhật cho nhân viên.
     * Tự động lưu trạng thái PENDING → SENT / FAILED vào DB.
     *
     * @param employee nhân viên cần gửi mail
     */
    void sendBirthdayEmail(Employee employee);
}
