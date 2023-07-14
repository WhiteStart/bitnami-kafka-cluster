package com.example.provider.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MyMailSender {

    private final JavaMailSender mailSender;

    public MyMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(from); // 设置邮件发送者地址
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
        } catch (MessagingException e) {
            // 处理邮件发送异常
            e.printStackTrace();
        }
    }
}
