package io.github.guennhatking.libra_auction.mappers;

import io.github.guennhatking.libra_auction.enums.transaction.TrangThaiVNPay;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDich;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayTransactionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper cho VNPay transactions
 */
@Component
public class VNPayMapper {
    
    public VNPayTransactionResponse toVNPayTransactionResponse(GiaoDich transaction, TrangThaiVNPay status) {
        if (transaction == null) {
            return null;
        }
        
        return new VNPayTransactionResponse(
            transaction.getId(),
            transaction.getMaGiaoDichCuaDoiTac(),
            transaction.getSoTien(),
            "", // Mô tả không lưu trong entity hiện tại
            "",
            status,
            transaction.getNgayTao().toString(),
            transaction.getNgayTao().toString()
        );
    }
}
