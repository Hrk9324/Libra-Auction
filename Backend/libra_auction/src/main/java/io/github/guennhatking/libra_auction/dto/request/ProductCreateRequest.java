package io.github.guennhatking.libra_auction.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    String tenTaiSan;
    Integer soLuong;
    String moTa;
    String danhMucId; // Category ID
}
