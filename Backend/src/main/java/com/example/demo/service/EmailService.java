package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.demo.entity.HoaDon;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@quancafepro.com}")
    private String fromEmail;

    @Value("${mail.from.name:QuanCaPhe Pro}")
    private String fromName;

    private String getFromAddress() {
        return fromName != null ? fromName + " <" + fromEmail + ">" : fromEmail;
    }

    /**
     * Send invoice email to customer.
     */
    public void sendInvoiceEmail(String to, HoaDon hoaDon, String customerName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Hóa đơn thanh toán - QuanCaPhe Pro #" + hoaDon.getMaHoaDon());

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("invoiceId", hoaDon.getMaHoaDon());
            context.setVariable("invoiceDate", hoaDon.getNgayGioTao()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("tableName", hoaDon.getBan() != null
                    ? hoaDon.getBan().getTenBan() : "N/A");
            context.setVariable("staffName", hoaDon.getNhanVien() != null
                    ? hoaDon.getNhanVien().getHoTen() : "N/A");

            BigDecimal total = hoaDon.getChiTietHoaDons() != null
                    ? hoaDon.getChiTietHoaDons().stream()
                            .map(ct -> ct.getThanhTien())
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            context.setVariable("total", total);
            context.setVariable("items", hoaDon.getChiTietHoaDons());

            String htmlContent = templateEngine.process("invoice-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Invoice email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send invoice email to: {}", to, e);
            throw new RuntimeException("Failed to send invoice email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending invoice email to: {}", to, e);
            throw new RuntimeException("Failed to send invoice email", e);
        }
    }

    /**
     * Send order confirmation email.
     */
    public void sendOrderConfirmation(String to, Long orderId,
                                      Map<com.example.demo.entity.ThucDon, Integer> items,
                                      BigDecimal total,
                                      int estimatedTime) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Xác nhận đơn hàng #" + orderId + " - QuanCaPhe Pro");

            Context context = new Context();
            context.setVariable("orderId", orderId);
            context.setVariable("items", items);
            context.setVariable("total", total);
            context.setVariable("estimatedTime", estimatedTime);
            context.setVariable("orderDate", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order confirmation sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation to: {}", to, e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending order confirmation to: {}", to, e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }

    /**
     * Send password reset email.
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Đặt lại mật khẩu - QuanCaPhe Pro");

            Context context = new Context();
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryHours", 24);

            String htmlContent = templateEngine.process("password-reset", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending password reset to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send welcome email for new OAuth2 users.
     */
    public void sendWelcomeEmail(String to, String username, String provider) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Chào mừng đến với QuanCaPhe Pro!");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("provider", provider);
            context.setVariable("loginUrl", "http://localhost:5173/login");

            String htmlContent = templateEngine.process("welcome-oauth2", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {} via {}", to, provider);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            throw new RuntimeException("Failed to send welcome email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending welcome email to: {}", to, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Send low stock alert to admin.
     */
    public void sendLowStockAlert(String to, List<Map<String, Object>> lowStockItems) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Cảnh báo tồn kho thấp - QuanCaPhe Pro");

            Context context = new Context();
            context.setVariable("lowStockItems", lowStockItems);
            context.setVariable("alertDate", LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            String htmlContent = templateEngine.process("low-stock-alert", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Low stock alert sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send low stock alert to: {}", to, e);
            throw new RuntimeException("Failed to send low stock alert", e);
        } catch (Exception e) {
            log.error("Unexpected error sending low stock alert to: {}", to, e);
            throw new RuntimeException("Failed to send low stock alert", e);
        }
    }

    /**
     * Send daily summary report to admin.
     */
    public void sendDailySummary(String to, Map<String, Object> summary) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject("Báo cáo ngày " + LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - QuanCaPhe Pro");

            Context context = new Context();
            context.setVariable("summary", summary);
            context.setVariable("date", LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            String htmlContent = templateEngine.process("daily-summary", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Daily summary sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send daily summary to: {}", to, e);
            throw new RuntimeException("Failed to send daily summary", e);
        } catch (Exception e) {
            log.error("Unexpected error sending daily summary to: {}", to, e);
            throw new RuntimeException("Failed to send daily summary", e);
        }
    }

    /**
     * Send a simple text email.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send simple email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending simple email to: {}", to, e);
            throw new RuntimeException("Failed to send simple email", e);
        }
    }
}
