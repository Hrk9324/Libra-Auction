package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.transaction.TransactionType;
import io.github.guennhatking.libra_auction.enums.transaction.TransactionStatus;
import io.github.guennhatking.libra_auction.models.auction.AuctionResult;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.auction.AuctionParticipationInfo;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.models.transaction.Transaction;
import io.github.guennhatking.libra_auction.models.transaction.DepositTransaction;
import io.github.guennhatking.libra_auction.models.transaction.PaymentTransaction;
import io.github.guennhatking.libra_auction.properties.VNPayProperties;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionParticipationInfoRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.DepositTransactionRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.TransactionRepository;
import io.github.guennhatking.libra_auction.repositories.transaction.PaymentTransactionRepository;
import io.github.guennhatking.libra_auction.repositories.product.ProductRepository;
import io.github.guennhatking.libra_auction.enums.product.ProductStatus;
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
 * Service xu ly thanh toan VNPay
 */
@Service
public class VNPayService {
    private final VNPayProperties vnPayProperties;
    private final DepositTransactionRepository depositTransactionRepository;
    private final AuctionParticipationInfoRepository participationInfoRepository;
    private final CustomerRepository customerRepository;
    private final AuctionRepository auctionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProductRepository productRepository;

    public VNPayService(VNPayProperties vnPayProperties,
            TransactionRepository transactionRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            CustomerRepository customerRepository,
            DepositTransactionRepository depositTransactionRepository,
            AuctionParticipationInfoRepository participationInfoRepository,
            AuctionRepository auctionRepository,
            ProductRepository productRepository) {
        this.vnPayProperties = vnPayProperties;
        this.customerRepository = customerRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.participationInfoRepository = participationInfoRepository;
        this.auctionRepository = auctionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public VNPayPaymentResponse createDeposit(VNPayDepositRequest request, String userId,
            HttpServletRequest servletRequest) {

        Customer user = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nguoi dung khong ton tai"));

        AuctionParticipationInfo participationInfo = participationInfoRepository
                .findByParticipantIdAndAuctionId(userId, request.auctionId())
                .orElseThrow(() -> new RuntimeException("Thong tin tham gia khong ton tai"));

        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(() -> new RuntimeException("Phien dau gia khong ton tai"));

        DepositTransaction deposit = new DepositTransaction(auction.getDepositAmount(),
                user, participationInfo);

        deposit.setCreatedAt(OffsetDateTime.now(ZoneOffset.ofHours(7)));
        deposit.setTransactionStatus(TransactionStatus.PROCESSING);

        // Luu de co ID (ma don hang)
        deposit = depositTransactionRepository.save(deposit);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Dat coc dau gia: " + request.auctionId();
        String vnp_TxnRef = deposit.getId();
        String vnp_IpAddr = getClientIp(servletRequest);
        String vnp_TmnCode = vnPayProperties.getTmnCode();

        long amount = auction.getDepositAmount() * 100; // VNPay tinh theo don vi xu (nhan 100)

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

        // 5. Tao chu ky so (Secure Hash)
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayProperties.getApiUrl() + "?" + queryUrl;

        // 6. Tra ve thong tin cho Frontend
        return new VNPayPaymentResponse(paymentUrl);
    }

    @Transactional
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, String userId,
            HttpServletRequest servletRequest) {
        Customer user = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nguoi dung khong ton tai"));

        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(() -> new RuntimeException("Phien dau gia khong ton tai"));

        AuctionResult auctionResult = auction.getAuctionResult();
        if (auctionResult == null || !auctionResult.getWinner().getId().equals(userId)) {
            throw new RuntimeException("Nguoi dung khong phai la nguoi thang cuoc cua phien dau gia nay");
        }

        Customer seller = auction.getCreator();

        PaymentTransaction payment = new PaymentTransaction(auction.getCurrentPrice(),
                user, seller, auctionResult);

        payment.setCreatedAt(OffsetDateTime.now(ZoneOffset.ofHours(7)));
        payment.setTransactionStatus(TransactionStatus.PROCESSING);

        // Luu de co ID (ma don hang)
        payment = paymentTransactionRepository.save(payment);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Dat coc dau gia: " + request.auctionId();
        String vnp_TxnRef = payment.getId();
        String vnp_IpAddr = getClientIp(servletRequest);
        String vnp_TmnCode = vnPayProperties.getTmnCode();

        long amount = auction.getDepositAmount() * 100; // VNPay tinh theo don vi xu (nhan 100)

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

        // 5. Tao chu ky so (Secure Hash)
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayProperties.getApiUrl() + "?" + queryUrl;

        // 6. Tra ve thong tin cho Frontend
        return new VNPayPaymentResponse(paymentUrl);
    }

    @Transactional
    public boolean depositSuccessed(VerifyPaymentRequest request) {
        // 1. Chuyen doi Record sang Map de tinh toan Checksum
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

        // 2. Kiem tra chu ky (Secure Hash)
        // Sap xep tham so theo alphabet (giong luc tao)
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Neu StringBuilder da co du lieu, them dau & truoc khi them tham so moi
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        String signValue = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        // So sanh hash tu VNPAY va hash minh tu tinh
        if (!signValue.equalsIgnoreCase(request.vnp_SecureHash())) {
            throw new RuntimeException("Chu ky khong hop le (Invalid Checksum)");
        }

        // 3. Tim giao dich dat coc trong DB
        DepositTransaction deposit = depositTransactionRepository.findById(request.vnp_TxnRef())
                .orElseThrow(() -> new RuntimeException("Khong tim thay giao dich dat coc"));

        // 4. Kiem tra so tien
        long vnpAmount = request.getActualAmount();
        long dbAmount = deposit.getAmount();
        if (vnpAmount != dbAmount) {
            throw new RuntimeException("So tien thanh toan khong khop");
        }

        // 5. Kiem tra trang thai giao dich
        if (deposit.getTransactionStatus() != TransactionStatus.PROCESSING) {
            // Giao dich nay da duoc cap nhat truoc do (boi IPN hoac lan verify truoc)
            return deposit.getTransactionStatus() == TransactionStatus.SUCCESS;
        }

        // 6. Cap nhat ket qua dua tren ResponseCode
        if ("00".equals(request.vnp_ResponseCode())) {
            // THANH CONG
            deposit.setTransactionStatus(TransactionStatus.SUCCESS);
            // Luu ma giao dich VNPAY de doi soat sau nay
            deposit.setPartnerTransactionId(request.vnp_TransactionNo());

            depositTransactionRepository.save(deposit);

            return true;
        } else {
            deposit.setTransactionStatus(TransactionStatus.FAILED);
            depositTransactionRepository.save(deposit);
            return false;
        }
    }

    @Transactional
    public boolean paymentSuccessed(VerifyPaymentRequest request) {
        // 1. Chuyen doi Record sang Map de tinh toan Checksum
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

        // 2. Kiem tra chu ky (Secure Hash)
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
        // So sanh hash tu VNPAY va hash minh tu tinh
        if (!signValue.equalsIgnoreCase(request.vnp_SecureHash())) {
            throw new RuntimeException("Chu ky khong hop le (Invalid Checksum)");
        }

        // 3. Tim giao dich thanh toan trong DB
        PaymentTransaction payment = paymentTransactionRepository.findById(request.vnp_TxnRef())
                .orElseThrow(() -> new RuntimeException("Khong tim thay giao dich thanh toan"));

        // 4. Kiem tra so tien
        long vnpAmount = request.getActualAmount();
        long dbAmount = payment.getAmount();
        if (vnpAmount != dbAmount) {
            throw new RuntimeException("So tien thanh toan khong khop");
        }

        // 5. Kiem tra trang thai giao dich
        if (payment.getTransactionStatus() != TransactionStatus.PROCESSING) {
            // Giao dich nay da duoc cap nhat truoc do (boi IPN hoac lan verify truoc)
            return payment.getTransactionStatus() == TransactionStatus.SUCCESS;
        }

        // 6. Cap nhat ket qua dua tren ResponseCode
        if ("00".equals(request.vnp_ResponseCode())) {
            // THANH CONG
            payment.setTransactionStatus(TransactionStatus.SUCCESS);
            // Luu ma giao dich VNPAY de doi soat sau nay
            payment.setPartnerTransactionId(request.vnp_TransactionNo());

            paymentTransactionRepository.save(payment);

            // Danh dau san pham da ban
            var product = payment.getAuctionResult().getAuction().getProduct();
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);

            return true;
        } else {
            payment.setTransactionStatus(TransactionStatus.FAILED);
            paymentTransactionRepository.save(payment);
            return false;
        }
    }

    public VNPayTransactionResponse getTransactionStatus(String transactionId) {
        Transaction transaction = depositTransactionRepository.findById(transactionId)
                .map(t -> (Transaction) t)
                .orElseGet(() -> paymentTransactionRepository.findById(transactionId)
                        .map(t -> (Transaction) t)
                        .orElseThrow(() -> new RuntimeException("Khong tim thay giao dich")));

        String description = "";
        String details = "";

        if (transaction.getTransactionType() == null) {
            throw new RuntimeException("Giao dich khong hop le");
        } else if (transaction.getTransactionType().equals(TransactionType.DEPOSIT)) {
            if (!(transaction instanceof DepositTransaction)) {
                throw new RuntimeException("Giao dich dat coc khong hop le");
            }
            description = "Dat coc dau gia";
            DepositTransaction deposit = (DepositTransaction) transaction;
            AuctionParticipationInfo info = deposit.getParticipationInfo();
            details = "Phien dau gia: " + info.getAuction().getId() + ", San pham: "
                    + info.getAuction().getProduct().getName();
        } else if (transaction.getTransactionType().equals(TransactionType.PAYMENT)) {
            if (!(transaction instanceof PaymentTransaction)) {
                throw new RuntimeException("Giao dich thanh toan khong hop le");
            }
            description = "Thanh toan dau gia";
            PaymentTransaction payment = (PaymentTransaction) transaction;
            details = "Phien dau gia: " + payment.getAuctionResult().getAuction().getId() + ", San pham: "
                    + payment.getAuctionResult().getAuction().getProduct().getName();
        }

        return new VNPayTransactionResponse(
                transaction.getId(),
                transaction.getPartnerTransactionId(),
                transaction.getAmount(),
                description,
                details,
                transaction.getTransactionStatus(),
                transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    @Transactional(readOnly = true)
    public boolean isDepositPaid(String userId, String auctionId) {
        return depositTransactionRepository
                .findByDepositorIdAndAuctionIdAndStatus(userId, auctionId, TransactionStatus.SUCCESS)
                .isPresent();
    }
}
