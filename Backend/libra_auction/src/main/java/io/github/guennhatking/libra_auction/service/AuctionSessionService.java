package io.github.guennhatking.libra_auction.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.github.guennhatking.libra_auction.dto.request.AuctionSessionCreateRequest;
import io.github.guennhatking.libra_auction.dto.request.AuctionSessionUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.AuctionSessionResponse;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.exception.AppException;
import io.github.guennhatking.libra_auction.exception.ErrorCode;
import io.github.guennhatking.libra_auction.mapper.AuctionSessionMapper;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.repos.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repos.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repos.TaiSanRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuctionSessionService {
    PhienDauGiaRepository phienDauGiaRepository;
    TaiSanRepository taiSanRepository;
    NguoiDungRepository nguoiDungRepository;
    AuctionSessionMapper auctionSessionMapper;

    public AuctionSessionResponse createAuctionSession(AuctionSessionCreateRequest request) {
        log.info("Creating new auction session");

        // Get current user ID from authentication context
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Current authenticated user ID: {}", userId);
        
        NguoiDung nguoiTao = nguoiDungRepository.findById(userId).orElse(null);
        if (nguoiTao == null) {
            log.error("User with ID {} not found", userId);
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Get product
        TaiSan taiSan = taiSanRepository.findById(request.getTaiSanId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Create auction session
        PhienDauGia phienDauGia = auctionSessionMapper.toAuctionSession(request);
        phienDauGia.setTaiSan(taiSan);
        phienDauGia.setNguoiTao(nguoiTao);
        phienDauGia.setThoiGianTao(LocalDateTime.now());
        phienDauGia.setTrangThaiPhien(Enums.TrangThaiPhien.CHUA_BAT_DAU);
        phienDauGia.setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet.CHUA_DUYET);

        PhienDauGia savedSession = phienDauGiaRepository.save(phienDauGia);
        return auctionSessionMapper.toAuctionSessionResponse(savedSession);
    }

    public AuctionSessionResponse getAuctionSession(String id) {
        log.info("Getting auction session with id: {}", id);

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return auctionSessionMapper.toAuctionSessionResponse(phienDauGia);
    }

    public List<AuctionSessionResponse> getAllAuctionSessions() {
        log.info("Getting all auction sessions");
        return phienDauGiaRepository.findAll().stream()
                .map(auctionSessionMapper::toAuctionSessionResponse)
                .toList();
    }

    public List<AuctionSessionResponse> getAuctionSessionsByStatus(Enums.TrangThaiPhien status) {
        log.info("Getting auction sessions by status: {}", status);
        return phienDauGiaRepository.findByTrangThaiPhien(status).stream()
                .map(auctionSessionMapper::toAuctionSessionResponse)
                .toList();
    }

    public List<AuctionSessionResponse> getAuctionSessionsByType(Enums.LoaiDauGia type) {
        log.info("Getting auction sessions by type: {}", type);
        return phienDauGiaRepository.findByLoaiDauGia(type).stream()
                .map(auctionSessionMapper::toAuctionSessionResponse)
                .toList();
    }

    public AuctionSessionResponse updateAuctionSession(String id, AuctionSessionUpdateRequest request) {
        log.info("Updating auction session with id: {}", id);

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Only allow updates if session hasn't started
        if (!phienDauGia.getTrangThaiPhien().equals(Enums.TrangThaiPhien.CHUA_BAT_DAU)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getThoiGianBatDau() != null) {
            phienDauGia.setThoiGianBatDau(request.getThoiGianBatDau());
        }
        if (request.getThoiLuong() > 0) {
            phienDauGia.setThoiLuong(request.getThoiLuong());
        }
        if (request.getGiaKhoiDiem() > 0) {
            phienDauGia.setGiaKhoiDiem(request.getGiaKhoiDiem());
        }
        if (request.getBuocGiaNhoNhat() > 0) {
            phienDauGia.setBuocGiaNhoNhat(request.getBuocGiaNhoNhat());
        }
        if (request.getTrangThaiPhien() != null) {
            phienDauGia.setTrangThaiPhien(request.getTrangThaiPhien());
        }

        PhienDauGia updatedSession = phienDauGiaRepository.save(phienDauGia);
        return auctionSessionMapper.toAuctionSessionResponse(updatedSession);
    }

    public void deleteAuctionSession(String id) {
        log.info("Deleting auction session with id: {}", id);

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        phienDauGiaRepository.delete(phienDauGia);
    }

    public AuctionSessionResponse approveAuctionSession(String id) {
        log.info("Approving auction session with id: {}", id);

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        phienDauGia.setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet.DA_DUYET);

        PhienDauGia approvedSession = phienDauGiaRepository.save(phienDauGia);
        return auctionSessionMapper.toAuctionSessionResponse(approvedSession);
    }

    public AuctionSessionResponse rejectAuctionSession(String id) {
        log.info("Rejecting auction session with id: {}", id);

        PhienDauGia phienDauGia = phienDauGiaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        phienDauGia.setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet.BI_TU_CHOI);

        PhienDauGia rejectedSession = phienDauGiaRepository.save(phienDauGia);
        return auctionSessionMapper.toAuctionSessionResponse(rejectedSession);
    }
}
