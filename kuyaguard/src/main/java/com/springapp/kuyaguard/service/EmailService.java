package com.springapp.kuyaguard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeMail(String email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Welcome to Kuya Guard, your trusted Authentication Platform!");
        message.setText("Hello, " + name + "!\n\nThank you so much for registering in Kuya Guard!\n\nWe will ensure that your account is always protected!\n\nIf you have any inquiries in using this application, please let us know and we will reach out to you as soon as possible!\n\nTake care always!\n\n\nRegards,\nKuya Guard's Team");
        mailSender.send(message);
    }

//    public void sendEmailOtp(String email, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(email);
//        message.setSubject("Kuya Guard: Password Reset One-Time Pin (OTP)");
//        message.setText("Magandang araw (Good day)! Here is the OTP for resetting your password:\n\n" + otp + "\n\nUse this to make the said changes and to update your account successfully.\n\nThank you so much!\n\nRegards,\nKuya Guard and Team");
//        mailSender.send(message);
//    }
//
//    public void sendOtpMail(String email, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(email);
//        message.setSubject("Kuya Guard: Account Verification OTP");
//        message.setText("Magandang araw (Good day)! Here is the OTP for verifying your account:\n\n" + otp + "\n\nUse this to complete the changes.\n\nThank you so much!\n\nRegards,\nKuya Guard and Team");
//        mailSender.send(message);
//    }

    public void sendEmailOtp(String toEmail, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("otp", otp);

        String process = templateEngine.process("verify-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Account Verification OTP");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }

    public void sendResetOtpEmail(String toEmail, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("otp", otp);

        String process = templateEngine.process("password-reset-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Forgot your password?");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }
}
