package com.example.provider.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Configuration
public class MailConfig {
    private final JavaMailSender mailSender;
    private final PropertiesConfig propertiesConfig;

    public MailConfig(JavaMailSender mailSender, PropertiesConfig propertiesConfig) {
        this.mailSender = mailSender;
        this.propertiesConfig = propertiesConfig;
    }

    protected void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(propertiesConfig.getFrom()); // 设置邮件发送者地址
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
        } catch (MessagingException e) {
            // 处理邮件发送异常
            e.printStackTrace();
        }
    }
}
