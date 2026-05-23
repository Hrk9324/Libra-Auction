package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.transaction.LoaiGiaoDich;
import io.github.guennhatking.libra_auction.enums.transaction.TinhTrangGiaoDich;
import io.github.guennhatking.libra_auction.models.auction.KetQuaDauGia;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.auction.ThongTinThamGiaDauGia;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDich;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDichDatCoc;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDichThanhToan;
import io.github.guennhatking.libra_auction.properties.VNPayProperties;
import io.github.guennhatking.libra_auction.repositories.auction.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.auction.ThongTinThamGiaDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.person.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.GiaoDichDatCocRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.GiaoDichRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.GiaoDichThanhToanRepository;
import io.github.guennhatking.libra_auction.utils.VNPayUtil;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayDepositRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.VerifyPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayPaymentResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayTransactionResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý thanh toán VNPay
 */
@Service
public class VNPayService {
    private final VNPayProperties vnPayProperties;
    private final GiaoDichDatCocRepository giaoDichDatCocRepository;
    private final ThongTinThamGiaDauGiaRepository thongTinThamGiaDauGiaRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhienDauGiaRepository phienDauGiaRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    public VNPayService(VNPayProperties vnPayProperties,
            GiaoDichRepository giaoDichRepository,
            GiaoDichThanhToanRepository giaoDichThanhToanRepository,
            NguoiDungRepository nguoiDungRepository,
            GiaoDichDatCocRepository giaoDichDatCocRepository,
            ThongTinThamGiaDauGiaRepository thongTinThamGiaDauGiaRepository,
            PhienDauGiaRepository phienDauGiaRepository) {
        this.vnPayProperties = vnPayProperties;
        this.nguoiDungRepository = nguoiDungRepository;
        this.giaoDichDatCocRepository = giaoDichDatCocRepository;
        this.thongTinThamGiaDauGiaRepository = thongTinThamGiaDauGiaRepository;
        this.phienDauGiaRepository = phienDauGiaRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
    }

    @Transactional
    public VNPayPaymentResponse createDeposit(VNPayDepositRequest request, String userId,
            HttpServletRequest servletRequest) {

        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        ThongTinThamGiaDauGia thongTinThamGiaDauGia = thongTinThamGiaDauGiaRepository
                .findByNguoiThamGiaIdAndPhienDauGiaId(userId, request.phienDauGiaId())
                .orElseThrow(() -> new RuntimeException("Thông tin tham gia không tồn tại"));

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(request.phienDauGiaId())
                .orElseThrow(() -> new RuntimeException("Phiên đấu giá không tồn tại"));

        GiaoDichDatCoc deposit = new GiaoDichDatCoc(phienDauGia.getTienCoc(),
                user, thongTinThamGiaDauGia);

        deposit.setNgayTao(OffsetDateTime.now(ZoneOffset.ofHours(7)));
        deposit.setTinhTrangGiaoDich(TinhTrangGiaoDich.DANG_XU_LY);

        // Lưu để có ID (mã đơn hàng)
        deposit = giaoDichDatCocRepository.save(deposit);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Dat coc dau gia: " + request.phienDauGiaId();
        String vnp_TxnRef = deposit.getId();
        String vnp_IpAddr = getClientIp(servletRequest);
        String vnp_TmnCode = vnPayProperties.getTmnCode();

        long amount = phienDauGia.getTienCoc() * 100; // VNPay tính theo đơn vị xu (nhân 100)

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 5. Tạo chữ ký số (Secure Hash)
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayProperties.getApiUrl() + "?" + queryUrl;

        // 6. Trả về thông tin cho Frontend
        return new VNPayPaymentResponse(paymentUrl);
    }

    @Transactional
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, String userId,
            HttpServletRequest servletRequest) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(request.phienDauGiaId())
                .orElseThrow(() -> new RuntimeException("Phiên đấu giá không tồn tại"));

        KetQuaDauGia ketQuaDauGia = phienDauGia.getKetQuaDauGia();
        if (ketQuaDauGia == null || !ketQuaDauGia.getNguoiThangDauGia().getId().equals(userId)) {
            throw new RuntimeException("Người dùng không phải là người thắng cuộc của phiên đấu giá này");
        }

        NguoiDung nguoiBan = phienDauGia.getNguoiTao();

        GiaoDichThanhToan payment = new GiaoDichThanhToan(phienDauGia.getGiaHienTai(),
                user, nguoiBan, ketQuaDauGia);

        payment.setNgayTao(OffsetDateTime.now(ZoneOffset.ofHours(7)));
        payment.setTinhTrangGiaoDich(TinhTrangGiaoDich.DANG_XU_LY);

        // Lưu để có ID (mã đơn hàng)
        payment = giaoDichThanhToanRepository.save(payment);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Dat coc dau gia: " + request.phienDauGiaId();
        String vnp_TxnRef = payment.getId();
        String vnp_IpAddr = getClientIp(servletRequest);
        String vnp_TmnCode = vnPayProperties.getTmnCode();

        long amount = phienDauGia.getTienCoc() * 100; // VNPay tính theo đơn vị xu (nhân 100)

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 5. Tạo chữ ký số (Secure Hash)
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayProperties.getApiUrl() + "?" + queryUrl;

        // 6. Trả về thông tin cho Frontend
        return new VNPayPaymentResponse(paymentUrl);
    }

    @Transactional
    public boolean depositSuccessed(VerifyPaymentRequest request) {
        // 1. Chuyển đổi Record sang Map để tính toán Checksum
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", request.vnp_Amount());
        fields.put("vnp_BankCode", request.vnp_BankCode());
        fields.put("vnp_BankTranNo", request.vnp_BankTranNo());
        fields.put("vnp_CardType", request.vnp_CardType());
        fields.put("vnp_OrderInfo", request.vnp_OrderInfo());
        fields.put("vnp_PayDate", request.vnp_PayDate());
        fields.put("vnp_ResponseCode", request.vnp_ResponseCode());
        fields.put("vnp_TmnCode", request.vnp_TmnCode());
        fields.put("vnp_TransactionNo", request.vnp_TransactionNo());
        fields.put("vnp_TransactionStatus", request.vnp_TransactionStatus());
        fields.put("vnp_TxnRef", request.vnp_TxnRef());

        // 2. Kiểm tra chữ ký (Secure Hash)
        // Sắp xếp tham số theo alphabet (giống như lúc tạo)
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Nếu StringBuilder đã có dữ liệu, thêm dấu & trước khi thêm tham số mới
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        String signValue = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        // So sánh hash từ VNPAY và hash mình tự tính
        if (!signValue.equalsIgnoreCase(request.vnp_SecureHash())) {
            throw new RuntimeException("Chữ ký không hợp lệ (Invalid Checksum)");
        }

        // 3. Tìm giao dịch đặt cọc trong DB
        GiaoDichDatCoc deposit = giaoDichDatCocRepository.findById(request.vnp_TxnRef())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt cọc"));

        // 4. Kiểm tra số tiền
        long vnpAmount = request.getActualAmount();
        long dbAmount = deposit.getSoTien();
        if (vnpAmount != dbAmount) {
            throw new RuntimeException("Số tiền thanh toán không khớp");
        }

        // 5. Kiểm tra trạng thái giao dịch
        if (deposit.getTinhTrangGiaoDich() != TinhTrangGiaoDich.DANG_XU_LY) {
            // Giao dịch này đã được cập nhật trước đó (bởi IPN hoặc lần verify trước)
            return deposit.getTinhTrangGiaoDich() == TinhTrangGiaoDich.THANH_CONG;
        }

        // 6. Cập nhật kết quả dựa trên ResponseCode
        if ("00".equals(request.vnp_ResponseCode())) {
            // THÀNH CÔNG
            deposit.setTinhTrangGiaoDich(TinhTrangGiaoDich.THANH_CONG);
            // Lưu mã giao dịch VNPAY để đối soát sau này
            deposit.setMaGiaoDichCuaDoiTac(request.vnp_TransactionNo());

            giaoDichDatCocRepository.save(deposit);

            return true;
        } else {
            deposit.setTinhTrangGiaoDich(TinhTrangGiaoDich.THAT_BAI);
            giaoDichDatCocRepository.save(deposit);
            return false;
        }
    }

    @Transactional
    public boolean paymentSuccessed(VerifyPaymentRequest request) {
        // 1. Chuyển đổi Record sang Map để tính toán Checksum
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", request.vnp_Amount());
        fields.put("vnp_BankCode", request.vnp_BankCode());
        fields.put("vnp_BankTranNo", request.vnp_BankTranNo());
        fields.put("vnp_CardType", request.vnp_CardType());
        fields.put("vnp_OrderInfo", request.vnp_OrderInfo());
        fields.put("vnp_PayDate", request.vnp_PayDate());
        fields.put("vnp_ResponseCode", request.vnp_ResponseCode());
        fields.put("vnp_TmnCode", request.vnp_TmnCode());
        fields.put("vnp_TransactionNo", request.vnp_TransactionNo());
        fields.put("vnp_TransactionStatus", request.vnp_TransactionStatus());
        fields.put("vnp_TxnRef", request.vnp_TxnRef());

        // 2. Kiểm tra chữ ký (Secure Hash)
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(fieldName)
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        }

        String signValue = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        // So sánh hash từ VNPAY và hash mình tự tính
        if (!signValue.equalsIgnoreCase(request.vnp_SecureHash())) {
            throw new RuntimeException("Chữ ký không hợp lệ (Invalid Checksum)");
        }

        // 3. Tìm giao dịch thanh toán trong DB
        GiaoDichThanhToan payment = giaoDichThanhToanRepository.findById(request.vnp_TxnRef())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán"));

        // 4. Kiểm tra số tiền
        long vnpAmount = request.getActualAmount();
        long dbAmount = payment.getSoTien();
        if (vnpAmount != dbAmount) {
            throw new RuntimeException("Số tiền thanh toán không khớp");
        }

        // 5. Kiểm tra trạng thái giao dịch
        if (payment.getTinhTrangGiaoDich() != TinhTrangGiaoDich.DANG_XU_LY) {
            // Giao dịch này đã được cập nhật trước đó (bởi IPN hoặc lần verify trước)
            return payment.getTinhTrangGiaoDich() == TinhTrangGiaoDich.THANH_CONG;
        }

        // 6. Cập nhật kết quả dựa trên ResponseCode
        if ("00".equals(request.vnp_ResponseCode())) {
            // THÀNH CÔNG
            payment.setTinhTrangGiaoDich(TinhTrangGiaoDich.THANH_CONG);
            // Lưu mã giao dịch VNPAY để đối soát sau này
            payment.setMaGiaoDichCuaDoiTac(request.vnp_TransactionNo());

            giaoDichThanhToanRepository.save(payment);

            return true;
        } else {
            payment.setTinhTrangGiaoDich(TinhTrangGiaoDich.THAT_BAI);
            giaoDichThanhToanRepository.save(payment);
            return false;
        }
    }

    public VNPayTransactionResponse getTransactionStatus(String transactionId) {
        GiaoDich giaoDich = giaoDichDatCocRepository.findById(transactionId)
                .map(gd -> (GiaoDich) gd)
                .orElseGet(() -> giaoDichThanhToanRepository.findById(transactionId)
                        .map(gd -> (GiaoDich) gd)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch")));

        String moTa = "";
        String thongTinChiTiet = "";

        if (giaoDich.getLoaiGiaoDich() == null) {
            throw new RuntimeException("Giao dịch không hợp lệ");
        } else if (giaoDich.getLoaiGiaoDich().equals(LoaiGiaoDich.DAT_COC)) {
            if (!(giaoDich instanceof GiaoDichDatCoc)) {
                throw new RuntimeException("Giao dịch đặt cọc không hợp lệ");
            }
            moTa = "Đặt cọc đấu giá";
            GiaoDichDatCoc datCoc = (GiaoDichDatCoc) giaoDich;
            ThongTinThamGiaDauGia thongTin = datCoc.getThongTinThamGia();
            thongTinChiTiet = "Phiên đấu giá: " + thongTin.getPhienDauGia().getId() + ", Sản phẩm: "
                    + thongTin.getPhienDauGia().getTaiSan().getTenTaiSan();
        } else if (giaoDich.getLoaiGiaoDich().equals(LoaiGiaoDich.THANH_TOAN)) {
            if (!(giaoDich instanceof GiaoDichThanhToan)) {
                throw new RuntimeException("Giao dịch thanh toán không hợp lệ");
            }
            moTa = "Thanh toán đấu giá";
            GiaoDichThanhToan thanhToan = (GiaoDichThanhToan) giaoDich;
            thongTinChiTiet = "Phiên đấu giá: " + thanhToan.getKetQuaDauGia().getPhienDauGia().getId() + ", Sản phẩm: "
                    + thanhToan.getKetQuaDauGia().getPhienDauGia().getTaiSan().getTenTaiSan();
        }

        return new VNPayTransactionResponse(
                giaoDich.getId(),
                giaoDich.getMaGiaoDichCuaDoiTac(),
                giaoDich.getSoTien(),
                moTa,
                thongTinChiTiet,
                giaoDich.getTinhTrangGiaoDich(),
                giaoDich.getNgayTao().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
