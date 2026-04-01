package io.github.guennhatking.libra_auction.dto.request;

import io.github.guennhatking.libra_auction.enums.Enums;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionSessionUpdateRequest {
    LocalDateTime thoiGianBatDau; // Start time
    long thoiLuong; // Duration in minutes
    long giaKhoiDiem; // Starting price
    long buocGiaNhoNhat; // Minimum bid step
    Enums.TrangThaiPhien trangThaiPhien; // Auction session status
}
