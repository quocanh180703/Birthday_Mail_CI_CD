package com.example.mailer.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;

// Utility class cho việc build MimeMessage.
// Tách biệt logic kỹ thuật của email ra khỏi business service,
// giúp tái sử dụng khi có thêm loại mail mới (nhắc nhở, thông báo...).

public final class EmailUtils {

    private EmailUtils() {
        // Utility class — không cho khởi tạo
    }

    /**
     * Build một MimeMessage HTML với tùy chọn đính kèm ảnh inline.
     *
     * @param sender      {@link JavaMailSender} dùng để tạo MimeMessage
     * @param to          địa chỉ email người nhận
     * @param subject     tiêu đề email
     * @param htmlBody    nội dung HTML của email
     * @param imagePath   đường dẫn file ảnh để đính kèm inline (null hoặc blank = không đính kèm)
     * @return            MimeMessage đã được cấu hình, sẵn sàng để gửi
     * @throws MessagingException nếu có lỗi khi build message
     */
    public static MimeMessage buildHtmlMessage(JavaMailSender sender,
                                               String to,
                                               String subject,
                                               String htmlBody,
                                               String imagePath) throws MessagingException {
        MimeMessage msg = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (hasValidImage(imagePath)) {
            FileSystemResource res = new FileSystemResource(new File(imagePath));
            helper.addInline("birthday_img", res);
        }

        return msg;
    }

    // Kiểm tra đường dẫn ảnh có hợp lệ và file tồn tại không.

    public static boolean hasValidImage(String imagePath) {
        return imagePath != null
                && !imagePath.isBlank()
                && new File(imagePath).exists();
    }
}
