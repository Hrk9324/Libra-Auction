package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.models.request.OtpRequest;
import io.github.guennhatking.libra_auction.repositories.request.OtpRequestRepository;
import io.github.guennhatking.libra_auction.enums.request.RequestStatus;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_MINUTES = 5;

    private final OtpRequestRepository otpRequestRepository;
    private final CustomerService customerService;

    public OtpService(OtpRequestRepository otpRequestRepository, CustomerService customerService) {
        this.otpRequestRepository = otpRequestRepository;
        this.customerService = customerService;
    }

    public String generateAndStore(String email) {
        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này."));
        
        String otp = generateOtp();
        
        OtpRequest otpRequest = new OtpRequest(customer);
        otpRequest.setMaOTPDaTao(otp);
        otpRequest.setTrangThaiYeuCau(RequestStatus.DANG_XU_LY);
        otpRequest.setThoiGianHetHanKichHoat(OffsetDateTime.now().plusMinutes(OTP_TTL_MINUTES));
        
        otpRequestRepository.save(otpRequest);
        return otp;
    }

    public boolean verify(String email, String otp) {
        OtpRequest otpRequest = otpRequestRepository.findLatestByEmail(email)
                .orElse(null);
        
        if (otpRequest == null) {
            return false;
        }
        
        // Check if OTP has expired
        if (OffsetDateTime.now().isAfter(otpRequest.getThoiGianHetHanKichHoat())) {
            return false;
        }
        
        // Check if OTP matches
        if (!otpRequest.getMaOTPDaTao().equals(otp)) {
            return false;
        }
        
        // Mark as used
        otpRequest.setTrangThaiYeuCau(RequestStatus.HOAN_THANH);
        otpRequest.setMaOTPNguoiDungNhap(otp);
        otpRequestRepository.save(otpRequest);
        
        return true;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", number);
    }
}
