package com.example.mailer.listener;

import com.example.mailer.config.RabbitConfig;
import com.example.mailer.model.Employee;
import com.example.mailer.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmployeeListener {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final MailService mailService;

    public EmployeeListener(MailService mailService) {
        this.mailService = mailService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receive(String payload) {
        try {
            Employee e = mapper.readValue(payload, Employee.class);
            if (e.getDob() == null) {
                System.out.println(">>> Nhân viên " + e.getName() + " không có ngày sinh (dob null)");
                return;
            }

            LocalDate today = LocalDate.now();

            System.out.println("--- KIỂM TRA SO SÁNH ---");
            System.out.println("Nhân viên: " + e.getName() + " | DOB: " + e.getDob());
            System.out.println("Ngày hệ thống Docker đang nhận: " + today);
            System.out.println("Tháng: " + e.getDob().getMonthValue() + " vs " + today.getMonthValue());
            System.out.println("Ngày: " + e.getDob().getDayOfMonth() + " vs " + today.getDayOfMonth());

            if (e.getDob().getMonthValue() == today.getMonthValue() &&
                    e.getDob().getDayOfMonth() == today.getDayOfMonth()) {
                System.out.println(">>> KHỚP NGÀY SINH NHẬT! Đang gọi MailService...");
                mailService.sendBirthdayEmail(e);
            } else {
                System.out.println(">>> KHÔNG KHỚP. Bỏ qua gửi mail.");
            }
        } catch (Exception ex) {
            System.err.println("Lỗi xử lý: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
