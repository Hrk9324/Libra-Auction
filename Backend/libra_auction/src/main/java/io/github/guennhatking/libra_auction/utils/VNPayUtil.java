package io.github.guennhatking.libra_auction.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class cho VNPay integration
 */
public class VNPayUtil {

    /**
     * Tính toán HMAC SHA-512
     */
    public static String hmacSHA512(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Tạo TxnRef - mã giao dịch duy nhất cho VNPay
     */
    public static String generateTxnRef(String transactionId) {
        long timestamp = System.currentTimeMillis() / 1000;
        return transactionId + "_" + timestamp;
    }

    /**
     * Format số tiền (VNPay yêu cầu nhân 100, không có dấu thập phân)
     */
    public static String formatAmount(long amount) {
        return String.valueOf(amount * 100);
    }

    /**
     * Tạo thời gian theo định dạng VNPay (yyyyMMddHHmmss)
     */
    public static String getCreateDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(new Date());
    }

    /**
     * Sắp xếp các tham số để tính toán secure hash
     */
    public static String buildSecureHash(Map<String, String> params, String hashSecret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (!"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
        }
        // Xóa ký tự & cuối cùng
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return hmacSHA512(hashSecret, sb.toString());
    }

    /**
     * Xác minh chữ ký từ VNPay callback
     */
    public static boolean verifyCallback(Map<String, String> params, String hashSecret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        String vnpSecureHash = params.get("vnp_SecureHash");
        String calculatedHash = buildSecureHash(params, hashSecret);
        return vnpSecureHash.equals(calculatedHash);
    }
}
