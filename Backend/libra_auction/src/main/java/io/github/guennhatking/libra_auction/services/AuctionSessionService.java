package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.models.ThongTinPhienDauGia;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.repositories.HinhAnhTaiSanRepository;
import io.github.guennhatking.libra_auction.repositories.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.TaiSanRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSessionCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSessionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionSessionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuctionSessionService {
    private final PhienDauGiaRepository phienDauGiaRepository;
    private final HinhAnhTaiSanRepository hinhAnhTaiSanRepository;
    private final TaiSanRepository taiSanRepository;

    public AuctionSessionService(PhienDauGiaRepository phienDauGiaRepository,
                                 HinhAnhTaiSanRepository hinhAnhTaiSanRepository,
                                 TaiSanRepository taiSanRepository) {
        this.phienDauGiaRepository = phienDauGiaRepository;
        this.hinhAnhTaiSanRepository = hinhAnhTaiSanRepository;
        this.taiSanRepository = taiSanRepository;
    }

    @Transactional(readOnly = true)
    public List<AuctionSessionResponse> getAuctionSessions() {
        return phienDauGiaRepository.findAll().stream()
            .sorted(Comparator.comparing(PhienDauGia::getThoiGianTao, Comparator.nullsLast(Comparator.reverseOrder())))
            .map(this::toAuctionSessionResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AuctionSessionResponse getAuctionSessionById(String id) {
        PhienDauGia session = phienDauGiaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));
        return toAuctionSessionResponse(session);
    }

    @Transactional
    public AuctionSessionResponse createAuctionSession(AuctionSessionCreateRequest request) {
        TaiSan product = taiSanRepository.findById(request.taiSanId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getThongTinPhienDauGia() != null) {
            throw new IllegalArgumentException("Product already has an auction session");
        }

        if (request.buocGiaNhoNhat() > request.giaKhoiDiem()) {
            throw new IllegalArgumentException("buocGiaNhoNhat must not exceed giaKhoiDiem");
        }

        ThongTinPhienDauGia auctionInfo = new ThongTinPhienDauGia(
            0L,
            request.giaKhoiDiem(),
            request.buocGiaNhoNhat(),
            product.getTenTaiSan(),
            product
        );

        PhienDauGia session = new PhienDauGia(
            null,
            auctionInfo,
            request.thoiGianBatDau(),
            request.giaKhoiDiem(),
            request.buocGiaNhoNhat()
        );
        session.setTaiSan(product);
        session.setThoiLuong(request.thoiLuong());
        session.setLoaiDauGia(request.loaiDauGia());
        session.setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet.CHUA_DUYET);
        session.setTrangThaiPhien(Enums.TrangThaiPhien.CHUA_BAT_DAU);

        PhienDauGia savedSession = phienDauGiaRepository.save(session);
        product.setThongTinPhienDauGia(savedSession.getThongTinPhienDauGia());

        return toAuctionSessionResponse(savedSession);
    }

    @Transactional
    public AuctionSessionResponse updateAuctionSession(String id, AuctionSessionUpdateRequest request) {
        PhienDauGia session = phienDauGiaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

        if (request.buocGiaNhoNhat() > request.giaKhoiDiem()) {
            throw new IllegalArgumentException("buocGiaNhoNhat must not exceed giaKhoiDiem");
        }

        session.setThoiGianBatDau(request.thoiGianBatDau());
        session.setThoiLuong(request.thoiLuong());
        session.setGiaKhoiDiem(request.giaKhoiDiem());
        session.setBuocGiaNhoNhat(request.buocGiaNhoNhat());
        session.setLoaiDauGia(request.loaiDauGia());

        PhienDauGia updatedSession = phienDauGiaRepository.save(session);
        return toAuctionSessionResponse(updatedSession);
    }

    @Transactional
    public void deleteAuctionSession(String id) {
        PhienDauGia session = phienDauGiaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));
        phienDauGiaRepository.delete(session);
    }

    private AuctionSessionResponse toAuctionSessionResponse(PhienDauGia session) {
        AuctionSessionResponse response = new AuctionSessionResponse();
        TaiSan product = session.getTaiSan();
        
        // Category info
        if (product != null && product.getDanhMuc() != null) {
            response.setCategory_id(product.getDanhMuc().getId());
            response.setCategory_name(product.getDanhMuc().getTenDanhMuc());
        } else {
            response.setCategory_id("uncategorized");
        }
        
        // Auction info
        response.setAuction_id(session.getId());
        response.setAuction_name(resolveAuctionTitle(session, product));
        response.setAuction_status(session.getTrangThaiPhien() != null ? session.getTrangThaiPhien().toString() : "CHUA_BAT_DAU");
        response.setAuction_type(session.getLoaiDauGia() != null ? session.getLoaiDauGia().toString() : "DAU_GIA_LEN");
        response.setStart_time(session.getThoiGianBatDau());
        response.setDuration(session.getThoiLuong());
        
        // Price info
        response.setStarting_price(session.getGiaKhoiDiem());
        response.setMin_bid_increment(session.getBuocGiaNhoNhat());
        response.setCurrent_price(session.getGiaKhoiDiem()); // Default to starting price if no bids
        
        // Product info
        if (product != null) {
            response.setProduct_id(product.getId());
            response.setProduct_name(product.getTenTaiSan());
            response.setQuantity(product.getSoLuong());
            response.setDescription(product.getMoTa());
            
            // Images
            if (product.getHinhAnhTaiSanList() != null && !product.getHinhAnhTaiSanList().isEmpty()) {
                List<String> imageUrls = product.getHinhAnhTaiSanList().stream()
                    .map(HinhAnhTaiSan::getHinhAnh)
                    .collect(Collectors.toList());
                response.setImages(imageUrls);
            }
            
            // Attributes
            if (product.getThuocTinhTaiSanList() != null && !product.getThuocTinhTaiSanList().isEmpty()) {
                List<AuctionSessionResponse.AttributeDTO> attributes = product.getThuocTinhTaiSanList().stream()
                    .map(attr -> new AuctionSessionResponse.AttributeDTO(attr.getTenThuocTinh(), attr.getGiaTri()))
                    .collect(Collectors.toList());
                response.setAttributes(attributes);
            }
        }
        
        // Bids info
        response.setTotal_bids(session.getLichSuDatGia() != null ? session.getLichSuDatGia().size() : 0);
        response.setTotal_participants(session.getDanhSachThamGia() != null ? session.getDanhSachThamGia().size() : 0);
        
        return response;
    }

    private String resolveAuctionTitle(PhienDauGia session, TaiSan product) {
        if (session.getThongTinPhienDauGia() != null && session.getThongTinPhienDauGia().getTieuDe() != null) {
            return session.getThongTinPhienDauGia().getTieuDe();
        }
        return product != null ? product.getTenTaiSan() : session.getId();
    }
}
