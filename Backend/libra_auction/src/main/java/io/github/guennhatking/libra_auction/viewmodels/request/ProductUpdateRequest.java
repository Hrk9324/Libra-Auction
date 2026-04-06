package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductUpdateRequest(
    @NotBlank(message = "tenTaiSan is required")
    String tenTaiSan,

    @Min(value = 1, message = "soLuong must be greater than 0")
    Integer soLuong,

    String moTa,

    @NotBlank(message = "danhMucId is required")
    String danhMucId,

    List<String> imageUrls
) {
}
