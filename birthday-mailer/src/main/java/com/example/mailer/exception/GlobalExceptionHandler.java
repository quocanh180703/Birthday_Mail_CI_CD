package com.example.mailer.exception;

import com.example.mailer.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Địa chỉ email không hợp lệ (định dạng sai hoặc domain không có MX record)
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailException ex) {
        log.warn("Email không hợp lệ: {}", ex.getEmail());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    // Status không hợp lệ (PENDING / SENT / FAILED)
    @ExceptionHandler(InvalidMailStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidMailStatusException ex) {
        log.warn("Yêu cầu với status không hợp lệ: {}", ex.getInvalidStatus());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    // Thiếu request parameter bắt buộc
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Thiếu request parameter: {}", ex.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request",
                        "Thiếu tham số bắt buộc: " + ex.getParameterName()));
    }

    //  Lỗi gửi mail sau tất cả các lần retry

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorResponse> handleMailSend(MailSendException ex) {
        log.error("Gửi mail thất bại cho '{}' sau {} lần thử: {}",
                ex.getRecipient(), ex.getAttempts(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(503, "Service Unavailable", ex.getMessage()));
    }

    // Lỗi lưu DB
 
    @ExceptionHandler(MailPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleMailPersistence(MailPersistenceException ex) {
        log.error("Lỗi database khi lưu mail record cho '{}': {}", ex.getRecipient(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error", ex.getMessage()));
    }

    // Fallback cho mọi exception chưa được xử lý

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Lỗi không xác định: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error", "Đã xảy ra lỗi không mong đợi"));
    }
}
