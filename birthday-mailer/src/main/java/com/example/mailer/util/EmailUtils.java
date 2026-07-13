package com.example.mailer.util;

import com.example.mailer.exception.InvalidEmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.File;
import java.util.Hashtable;

// Utility class cho việc build MimeMessage và validate địa chỉ email.
// Tách biệt logic kỹ thuật của email ra khỏi business service,
// giúp tái sử dụng khi có thêm loại mail mới (nhắc nhở, thông báo...).

public final class EmailUtils {

    private EmailUtils() {
        // Utility class — không cho khởi tạo
    }

    /**
     * Validate địa chỉ email: kiểm tra định dạng và DNS MX record của domain.
     * Ném {@link InvalidEmailException} nếu không hợp lệ.
     *
     * @param email địa chỉ email cần kiểm tra
     * @throws InvalidEmailException nếu định dạng sai hoặc domain không có MX record
     */
    public static void validateEmail(String email) {
        // 1. Kiểm tra định dạng cơ bản
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException(email, "địa chỉ email không được để trống");
        }
        try {
            InternetAddress addr = new InternetAddress(email);
            addr.validate();
        } catch (AddressException ex) {
            throw new InvalidEmailException(email, "định dạng không hợp lệ");
        }

        // 2. Kiểm tra domain có MX record không (DNS lookup)
        String domain = email.substring(email.indexOf('@') + 1);
        if (!hasMxRecord(domain)) {
            throw new InvalidEmailException(email,
                    String.format("domain '%s' không có MX record — email không thể giao được", domain));
        }
    }

    /**
     * Kiểm tra domain có MX record trong DNS không.
     *
     * @param domain tên domain cần kiểm tra
     * @return true nếu tìm thấy ít nhất 1 MX record
     */
    public static boolean hasMxRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * Build một MimeMessage HTML với tùy chọn đính kèm ảnh inline.
     *
     * @param sender    {@link JavaMailSender} dùng để tạo MimeMessage
     * @param to        địa chỉ email người nhận
     * @param subject   tiêu đề email
     * @param htmlBody  nội dung HTML của email
     * @param imagePath đường dẫn file ảnh để đính kèm inline (null hoặc blank = không đính kèm)
     * @return MimeMessage đã được cấu hình, sẵn sàng để gửi
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
