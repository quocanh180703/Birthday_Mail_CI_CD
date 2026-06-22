package com.example.mailer.listener;

import com.example.mailer.config.RabbitConfig;
import com.example.mailer.exception.MailPersistenceException;
import com.example.mailer.exception.MailSendException;
import com.example.mailer.model.Employee;
import com.example.mailer.service.IMailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class EmployeeListener {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final IMailService mailService;

    public EmployeeListener(IMailService mailService) {
        this.mailService = mailService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receive(String payload) {
        try {
            Employee e = mapper.readValue(payload, Employee.class);

            if (e.getDob() == null) {
                log.warn("Nhân viên '{}' không có ngày sinh (dob null), bỏ qua.", e.getName());
                return;
            }

            LocalDate today = LocalDate.now();
            log.debug("Kiểm tra sinh nhật — nhân viên: {}, DOB: {}, hôm nay: {}", e.getName(), e.getDob(), today);

            if (e.getDob().getMonthValue() == today.getMonthValue()
                    && e.getDob().getDayOfMonth() == today.getDayOfMonth()) {
                log.info("Khớp ngày sinh nhật cho '{}', bắt đầu gửi mail.", e.getName());
                mailService.sendBirthdayEmail(e);
            } else {
                log.debug("Không khớp ngày sinh nhật cho '{}', bỏ qua.", e.getName());
            }

        } catch (MailSendException ex) {
            // Lỗi gửi mail đã được xử lý và log ở MailServiceImpl, chỉ log thêm ở đây để trace
            log.error("Listener: gửi mail thất bại cho '{}' — {}", ex.getRecipient(), ex.getMessage());
        } catch (MailPersistenceException ex) {
            log.error("Listener: lỗi database khi xử lý mail cho '{}' — {}", ex.getRecipient(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Listener: lỗi không xác định khi xử lý message: {}", ex.getMessage(), ex);
        }
    }
}
