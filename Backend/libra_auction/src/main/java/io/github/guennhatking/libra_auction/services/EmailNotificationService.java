package io.github.guennhatking.libra_auction.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.person.Customer;

import java.time.format.DateTimeFormatter;

/**
 * Service to send email notifications for auction events
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@auction.com}")
    private String fromEmail;

    @Value("${app.auction.name:Libra Auction}")
    private String auctionName;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send notification when an auction has started
     * @param auction The auction
     */
    public void sendAuctionStartedNotification(Auction auction) {
        try {
            String productName = auction.getProduct() != null ? auction.getProduct().getName() : "N/A";
            String auctionId = auction.getId();
            String startTime = auction.getStartTime().format(DATE_FORMATTER);

            // For now, we send to the auction creator
            if (auction.getCreator() != null && auction.getCreator().getEmail() != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(auction.getCreator().getEmail());
                message.setSubject("[" + auctionName + "] Phien dau gia cua ban da bat dau");
                message.setText(buildAuctionStartedEmailBody(productName, auctionId, startTime));

                mailSender.send(message);
                logger.info("Auction started notification sent to {}", auction.getCreator().getEmail());
            }
        } catch (Exception e) {
            logger.error("Error sending auction started notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification to the auction winner
     * @param auction The auction
     * @param winner The winner user
     * @param finalPrice The final winning price
     */
    public void sendAuctionWinnerNotification(Auction auction, Customer winner, Long finalPrice) {
        try {
            if (winner == null || winner.getEmail() == null) {
                logger.warn("Cannot send winner notification: winner email is null");
                return;
            }

            String productName = auction.getProduct() != null ? auction.getProduct().getName() : "N/A";
            String auctionId = auction.getId();
            String endTime = auction.getStartTime().plusSeconds(auction.getDuration()).format(DATE_FORMATTER);
            String formattedPrice = String.format("%,d", finalPrice);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(winner.getEmail());
            message.setSubject("[" + auctionName + "] Xin chuc mung! Ban da thang cuoc dau gia");
            message.setText(buildWinnerNotificationEmailBody(productName, auctionId, endTime, formattedPrice));

            mailSender.send(message);
            logger.info("Winner notification sent to {}", winner.getEmail());
        } catch (Exception e) {
            logger.error("Error sending winner notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when an auction has ended
     * @param auction The auction
     */
    public void sendAuctionEndedNotification(Auction auction) {
        try {
            if (auction.getCreator() == null || auction.getCreator().getEmail() == null) {
                logger.warn("Cannot send auction ended notification: creator email is null");
                return;
            }

            String productName = auction.getProduct() != null ? auction.getProduct().getName() : "N/A";
            String auctionId = auction.getId();
            String endTime = auction.getStartTime().plusSeconds(auction.getDuration()).format(DATE_FORMATTER);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(auction.getCreator().getEmail());
            message.setSubject("[" + auctionName + "] Phien dau gia cua ban da ket thuc");
            message.setText(buildAuctionEndedEmailBody(productName, auctionId, endTime));

            mailSender.send(message);
            logger.info("Auction ended notification sent to {}", auction.getCreator().getEmail());
        } catch (Exception e) {
            logger.error("Error sending auction ended notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment request notification to the winner
     * @param auction The auction
     * @param winner The winner
     * @param amount The amount to pay
     */
    public void sendPaymentRequestNotification(Auction auction, Customer winner, Long amount) {
        try {
            if (winner == null || winner.getEmail() == null) {
                logger.warn("Cannot send payment request notification: winner email is null");
                return;
            }

            String productName = auction.getProduct() != null ? auction.getProduct().getName() : "N/A";
            String formattedPrice = String.format("%,d", amount);
            String auctionId = auction.getId();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(winner.getEmail());
            message.setSubject("[" + auctionName + "] Yeu cau thanh toan san pham da thang");
            message.setText(buildPaymentRequestEmailBody(productName, formattedPrice, auctionId));

            mailSender.send(message);
            logger.info("Payment request notification sent to {}", winner.getEmail());
        } catch (Exception e) {
            logger.error("Error sending payment request notification: {}", e.getMessage(), e);
        }
    }

    public void sendEmailVerificationOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[" + auctionName + "] Ma xac thuc email cua ban");
            message.setText(buildEmailVerificationOtpBody(otp));
            mailSender.send(message);
            logger.info("Email verification OTP sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending email verification OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Khong the gui email. Vui long thu lai.");
        }
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[" + auctionName + "] Ma dat lai mat khau cua ban");
            message.setText(buildPasswordResetOtpBody(otp));
            mailSender.send(message);
            logger.info("Password reset OTP sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending password reset OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Khong the gui email. Vui long thu lai.");
        }
    }

    public void sendAuctionCancelledNotification(Auction auction, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            String productName = auction.getProduct() != null ? auction.getProduct().getName() : "N/A";
            String recipientEmail = auction.getCreator() != null ? auction.getCreator().getEmail() : null;
            if (recipientEmail == null) return;
            message.setTo(recipientEmail);
            message.setSubject("[" + auctionName + "] Phien dau gia bi huy");
            message.setText("Xin chao,\n\n" +
                    "Phien dau gia '" + productName + "' (ID: " + auction.getId() + ") da bi huy.\n\n" +
                    "Ly do: " + (reason != null ? reason : "Khong co ly do cu the") + "\n\n" +
                    "San pham se duoc tro ve trang thai san sang. Ban co the tao phien dau gia moi.\n\n" +
                    "Tran trong,\n" + auctionName);
            mailSender.send(message);
            logger.info("Auction cancelled email sent for auction {}", auction.getId());
        } catch (Exception e) {
            logger.error("Error sending auction cancelled email: {}", e.getMessage(), e);
        }
    }

    // ==================== Email Body Templates ====================

    private String buildAuctionStartedEmailBody(String productName, String auctionId, String startTime) {
        return "Xin chao,\n\n" +
               "Phien dau gia cua ban vua bat dau!\n\n" +
               "San pham: " + productName + "\n" +
               "ID Phien: " + auctionId + "\n" +
               "Thoi gian bat dau: " + startTime + "\n\n" +
               "Nguoi mua se co the dat gia cho san pham cua ban ngay bay gio.\n\n" +
               "Cam on ban da su dung " + auctionName + "!\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }

    private String buildWinnerNotificationEmailBody(String productName, String auctionId, String endTime, String price) {
        return "Xin chao,\n\n" +
               "Chuc mung! Ban da thang cuoc dau gia!\n\n" +
               "San pham: " + productName + "\n" +
               "ID Phien: " + auctionId + "\n" +
               "Gia thang: " + price + " VND\n" +
               "Thoi gian ket thuc: " + endTime + "\n\n" +
               "Vui long tien hanh thanh toan trong vong 3 ngay.\n\n" +
               "Cam on ban da su dung " + auctionName + "!\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }

    private String buildAuctionEndedEmailBody(String productName, String auctionId, String endTime) {
        return "Xin chao,\n\n" +
               "Phien dau gia cua ban da ket thuc.\n\n" +
               "San pham: " + productName + "\n" +
               "ID Phien: " + auctionId + "\n" +
               "Thoi gian ket thuc: " + endTime + "\n\n" +
               "Hay kiem tra chi tiet ket qua tren he thong cua chung toi.\n\n" +
               "Cam on ban da su dung " + auctionName + "!\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }

    private String buildPaymentRequestEmailBody(String productName, String price, String auctionId) {
        return "Xin chao,\n\n" +
               "Ban can thanh toan cho san pham ma ban da thang cuoc.\n\n" +
               "San pham: " + productName + "\n" +
               "So tien can thanh toan: " + price + " VND\n" +
               "ID Phien: " + auctionId + "\n\n" +
               "Vui long thanh toan trong vong 3 ngay de hoan thanh giao dich.\n\n" +
               "Cam on ban da su dung " + auctionName + "!\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }

    private String buildEmailVerificationOtpBody(String otp) {
        return "Xin chao,\n\n" +
               "Ma xac thuc email cua ban la:\n\n" +
               "    " + otp + "\n\n" +
               "Ma nay co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.\n\n" +
               "Neu ban khong yeu cau xac thuc email, hay bo qua email nay.\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }

    private String buildPasswordResetOtpBody(String otp) {
        return "Xin chao,\n\n" +
               "Chung toi nhan duoc yeu cau dat lai mat khau cho tai khoan cua ban.\n\n" +
               "Ma OTP dat lai mat khau:\n\n" +
               "    " + otp + "\n\n" +
               "Ma nay co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.\n\n" +
               "Neu ban khong yeu cau dat lai mat khau, hay bo qua email nay.\n\n" +
               "Tran trong,\n" +
               auctionName + " Team";
    }
}
