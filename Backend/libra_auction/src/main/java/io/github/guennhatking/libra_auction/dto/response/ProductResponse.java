package io.github.guennhatking.libra_auction.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String tenTaiSan;
    Integer soLuong;
    String moTa;
    String danhMucId;
    String danhMucName;
}
