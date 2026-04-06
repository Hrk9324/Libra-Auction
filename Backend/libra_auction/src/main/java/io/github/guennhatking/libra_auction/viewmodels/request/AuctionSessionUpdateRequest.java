package io.github.guennhatking.libra_auction.viewmodels.request;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AuctionSessionUpdateRequest(
    @NotNull(message = "thoiGianBatDau is required")
    LocalDateTime thoiGianBatDau,

    @NotNull(message = "thoiLuong is required")
    @Positive(message = "thoiLuong must be greater than 0")
    Long thoiLuong,

    @NotNull(message = "giaKhoiDiem is required")
    @Positive(message = "giaKhoiDiem must be greater than 0")
    Long giaKhoiDiem,

    @NotNull(message = "buocGiaNhoNhat is required")
    @Positive(message = "buocGiaNhoNhat must be greater than 0")
    Long buocGiaNhoNhat,

    @NotNull(message = "loaiDauGia is required")
    Enums.LoaiDauGia loaiDauGia
) {
}
