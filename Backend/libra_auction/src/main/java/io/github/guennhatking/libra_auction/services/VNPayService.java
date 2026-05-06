package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.transaction.LoaiGiaoDich;
import io.github.guennhatking.libra_auction.enums.transaction.TinhTrangGiaoDich;
import io.github.guennhatking.libra_auction.enums.transaction.TrangThaiVNPay;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDichThanhToan;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDich;
import io.github.guennhatking.libra_auction.properties.VNPayProperties;
import io.github.guennhatking.libra_auction.repositories.person.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.GiaoDichRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.GiaoDichThanhToanRepository;
import io.github.guennhatking.libra_auction.utils.VNPayUtil;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayPaymentResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayTransactionResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý thanh toán VNPay
 */
@Service
public class VNPayService {

    private final VNPayProperties vnPayProperties;
    private final GiaoDichRepository giaoDichRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final NguoiDungRepository nguoiDungRepository;

    // Cache để lưu trạng thái thanh toán (trong thực tế nên dùng Redis)
    private final Map<String, TrangThaiVNPay> paymentStatusCache = new HashMap<>();

    public VNPayService(VNPayProperties vnPayProperties,
                        GiaoDichRepository giaoDichRepository,
                        GiaoDichThanhToanRepository giaoDichThanhToanRepository,
                        NguoiDungRepository nguoiDungRepository) {
        this.vnPayProperties = vnPayProperties;
        this.giaoDichRepository = giaoDichRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    /**
     * Tạo thanh toán VNPay
     */
    @Transactional
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, String userId) {
        // Lấy thông tin user
        Optional<NguoiDung> userOptional = nguoiDungRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }
        
        NguoiDung user = userOptional.get();
        
        // Tạo giao dịch trong database
        GiaoDichThanhToan transaction = new GiaoDichThanhToan(
            request.soTien(),
            user,
            user  // Người nhận là chính người dùng (hệ thống)
        );
        transaction.setTinhTrangGiaoDich(TinhTrangGiaoDich.DANG_XU_LY);
        
        GiaoDichThanhToan savedTransaction = giaoDichThanhToanRepository.save(transaction);
        
        // Tạo TxnRef cho VNPay
        String txnRef = VNPayUtil.generateTxnRef(savedTransaction.getId());
        savedTransaction.setMaGiaoDichCuaDoiTac(txnRef);
        giaoDichThanhToanRepository.save(savedTransaction);
        
        // Lưu trạng thái thanh toán
        paymentStatusCache.put(savedTransaction.getId(), TrangThaiVNPay.DANG_THANH_TOAN);
        
        // Tạo params cho VNPay
        Map<String, String> params = buildVNPayParams(
            txnRef,
            request.soTien(),
            request.orderInfo(),
            user.getId()
        );
        
        // Tính toán secure hash
        String secureHash;
        try {
            secureHash = VNPayUtil.buildSecureHash(params, vnPayProperties.getHashSecret());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi khi tính toán secure hash: " + e.getMessage());
        }
        
        params.put("vnp_SecureHash", secureHash);
        params.put("vnp_SecureHashType", "SHA512");
        
        // Xây dựng URL thanh toán
        String paymentUrl = buildPaymentUrl(params);
        
        return new VNPayPaymentResponse(
            paymentUrl,
            savedTransaction.getId(),
            txnRef,
            request.soTien(),
            request.orderInfo()
        );
    }

    /**
     * Xử lý callback từ VNPay
     */
    @Transactional
    public void handleCallback(Map<String, String> params) throws NoSuchAlgorithmException, InvalidKeyException {
        // Xác minh chữ ký
        if (!VNPayUtil.verifyCallback(params, vnPayProperties.getHashSecret())) {
            throw new RuntimeException("Chữ ký VNPay không hợp lệ");
        }
        
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        
        // Tìm giao dịch theo TxnRef
        // Trong thực tế, cần có một repository method để tìm theo maGiaoDichCuaDoiTac
        Optional<GiaoDich> transactionOptional = giaoDichRepository.findAll().stream()
            .filter(t -> txnRef.equals(t.getMaGiaoDichCuaDoiTac()))
            .findFirst();
        
        if (transactionOptional.isEmpty()) {
            throw new RuntimeException("Giao dịch không tồn tại: " + txnRef);
        }
        
        GiaoDich transaction = transactionOptional.get();
        
        // Cập nhật trạng thái giao dịch
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            transaction.setTinhTrangGiaoDich(TinhTrangGiaoDich.THANH_CONG);
            paymentStatusCache.put(transaction.getId(), TrangThaiVNPay.THANH_TOAN_THANH_CONG);
        } else {
            // Thanh toán thất bại
            transaction.setTinhTrangGiaoDich(TinhTrangGiaoDich.THAT_BAI);
            paymentStatusCache.put(transaction.getId(), TrangThaiVNPay.THANH_TOAN_THAT_BAI);
        }
        
        giaoDichRepository.save(transaction);
    }

    /**
     * Lấy thông tin giao dịch thanh toán
     */
    @Transactional(readOnly = true)
    public VNPayTransactionResponse getTransaction(String transactionId) {
        Optional<GiaoDich> transactionOptional = giaoDichRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            throw new RuntimeException("Giao dịch không tồn tại");
        }
        
        GiaoDich transaction = transactionOptional.get();
        TrangThaiVNPay status = paymentStatusCache.getOrDefault(transactionId, TrangThaiVNPay.DANG_THANH_TOAN);
        
        return new VNPayTransactionResponse(
            transaction.getId(),
            transaction.getMaGiaoDichCuaDoiTac(),
            transaction.getSoTien(),
            "", // Mô tả không lưu trong GiaoDich hiện tại
            "",
            status,
            transaction.getNgayTao().toString(),
            OffsetDateTime.now(ZoneOffset.ofHours(7)).toString()
        );
    }

    /**
     * Hủy thanh toán
     */
    @Transactional
    public void cancelPayment(String transactionId) {
        Optional<GiaoDich> transactionOptional = giaoDichRepository.findById(transactionId);
        if (transactionOptional.isEmpty()) {
            throw new RuntimeException("Giao dịch không tồn tại");
        }
        
        GiaoDich transaction = transactionOptional.get();
        transaction.setTinhTrangGiaoDich(TinhTrangGiaoDich.THAT_BAI);
        paymentStatusCache.put(transactionId, TrangThaiVNPay.DA_HUY);
        
        giaoDichRepository.save(transaction);
    }

    /**
     * Xây dựng các tham số cho VNPay
     */
    private Map<String, String> buildVNPayParams(String txnRef, long amount, String orderInfo, String userId) {
        Map<String, String> params = new HashMap<>();
        
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", VNPayUtil.formatAmount(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Thanh toan don hang");
        params.put("vnp_Locale", "vn");
        params.put("vnp_CreateDate", VNPayUtil.getCreateDate());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        
        return params;
    }

    /**
     * Xây dựng URL thanh toán
     */
    private String buildPaymentUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder(vnPayProperties.getApiUrl());
        url.append("?");
        
        params.forEach((key, value) -> {
            try {
                url.append(key).append("=")
                   .append(java.net.URLEncoder.encode(value, "UTF-8"))
                   .append("&");
            } catch (java.io.UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
        
        // Xóa ký tự & cuối cùng
        if (url.length() > 0) {
            url.deleteCharAt(url.length() - 1);
        }
        
        return url.toString();
    }
}
