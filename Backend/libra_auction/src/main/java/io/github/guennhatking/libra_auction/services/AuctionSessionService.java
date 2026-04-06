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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            .map(session -> {
                TaiSan product = session.getTaiSan();
                String categoryId = product != null && product.getDanhMuc() != null ? product.getDanhMuc().getId() : "uncategorized";

                return new AuctionSessionResponse(
                    session.getId(),
                    product != null ? resolveProductImage(product) : null,
                    resolveAuctionTitle(session, product),
                    session.getGiaKhoiDiem(),
                    session.getLichSuDatGia() != null ? session.getLichSuDatGia().size() : 0,
                    calculateTimeLeft(session),
                    "/auctions/" + categoryId + "/" + session.getId()
                );
            })
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

    private String resolveProductImage(TaiSan product) {
        Optional<HinhAnhTaiSan> firstImage = hinhAnhTaiSanRepository.findByTaiSanIdOrderByThuTuHienThiAsc(product.getId()).stream().findFirst();
        return firstImage.map(HinhAnhTaiSan::getHinhAnh)
            .orElseGet(() -> product.getDanhMuc() != null ? product.getDanhMuc().getHinhAnh() : null);
    }

    private AuctionSessionResponse toAuctionSessionResponse(PhienDauGia session) {
        TaiSan product = session.getTaiSan();
        String categoryId = product != null && product.getDanhMuc() != null ? product.getDanhMuc().getId() : "uncategorized";

        return new AuctionSessionResponse(
            session.getId(),
            product != null ? resolveProductImage(product) : null,
            resolveAuctionTitle(session, product),
            session.getGiaKhoiDiem(),
            session.getLichSuDatGia() != null ? session.getLichSuDatGia().size() : 0,
            calculateTimeLeft(session),
            "/auctions/" + categoryId + "/" + session.getId()
        );
    }

    private String resolveAuctionTitle(PhienDauGia session, TaiSan product) {
        if (session.getThongTinPhienDauGia() != null && session.getThongTinPhienDauGia().getTieuDe() != null) {
            return session.getThongTinPhienDauGia().getTieuDe();
        }
        return product != null ? product.getTenTaiSan() : session.getId();
    }

    private long calculateTimeLeft(PhienDauGia session) {
        LocalDateTime startTime = session.getThoiGianBatDau();
        if (startTime == null) {
            return 0L;
        }

        LocalDateTime endTime = startTime.plusSeconds(session.getThoiLuong());
        long millis = Duration.between(LocalDateTime.now(), endTime).toMillis();
        return Math.max(millis, 0L);
    }
}
