package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.models.DanhMuc;
import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.models.ThongTinPhienDauGia;
import io.github.guennhatking.libra_auction.repositories.DanhMucRepository;
import io.github.guennhatking.libra_auction.repositories.HinhAnhTaiSanRepository;
import io.github.guennhatking.libra_auction.repositories.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.TaiSanRepository;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PublicApiController {
    private final DanhMucRepository danhMucRepository;
    private final TaiSanRepository taiSanRepository;
    private final PhienDauGiaRepository phienDauGiaRepository;
    private final HinhAnhTaiSanRepository hinhAnhTaiSanRepository;

    public PublicApiController(DanhMucRepository danhMucRepository,
                               TaiSanRepository taiSanRepository,
                               PhienDauGiaRepository phienDauGiaRepository,
                               HinhAnhTaiSanRepository hinhAnhTaiSanRepository) {
        this.danhMucRepository = danhMucRepository;
        this.taiSanRepository = taiSanRepository;
        this.phienDauGiaRepository = phienDauGiaRepository;
        this.hinhAnhTaiSanRepository = hinhAnhTaiSanRepository;
    }

    @GetMapping("/categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return danhMucRepository.findAll().stream()
            .map(category -> new CategoryResponse(
                category.getId(),
                category.getHinhAnh(),
                category.getTenDanhMuc(),
                "/auctions/" + category.getId()
            ))
            .toList();
    }

    @GetMapping("/products")
    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return taiSanRepository.findAll().stream()
            .map(product -> {
                ThongTinPhienDauGia auctionInfo = product.getThongTinPhienDauGia();
                long startingBid = auctionInfo != null ? auctionInfo.getGiaKhoiDiem() : 0L;
                String categoryId = product.getDanhMuc() != null ? product.getDanhMuc().getId() : "uncategorized";

                return new ProductResponse(
                    product.getId(),
                    resolveProductImage(product),
                    product.getTenTaiSan(),
                    startingBid,
                    0,
                    null,
                    "/auctions/" + categoryId + "/" + product.getId()
                );
            })
            .toList();
    }

    @PostMapping("/products")
    @Transactional
    public ProductResponse createProduct(@Valid @RequestBody ProductCreateRequest request) {
        DanhMuc category = danhMucRepository.findById(request.danhMucId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        TaiSan product = new TaiSan(
            request.tenTaiSan(),
            request.soLuong(),
            request.moTa(),
            category
        );

        TaiSan savedProduct = taiSanRepository.save(product);

        return new ProductResponse(
            savedProduct.getId(),
            resolveProductImage(savedProduct),
            savedProduct.getTenTaiSan(),
            0L,
            0,
            null,
            "/auctions/" + category.getId() + "/" + savedProduct.getId()
        );
    }

    @GetMapping("/auction-sessions")
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

    private String resolveProductImage(TaiSan product) {
        Optional<HinhAnhTaiSan> firstImage = hinhAnhTaiSanRepository.findByTaiSanIdOrderByThuTuHienThiAsc(product.getId()).stream().findFirst();
        return firstImage.map(HinhAnhTaiSan::getHinhAnh).orElseGet(() -> product.getDanhMuc() != null ? product.getDanhMuc().getHinhAnh() : null);
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

    public record CategoryResponse(
        String id,
        String image_src,
        String title,
        String href
    ) {
    }

    public record ProductResponse(
        String id,
        String image_src,
        String title,
        long starting_bid,
        int biders,
        LocalDateTime starting_date,
        String href
    ) {
    }

    public record ProductCreateRequest(
        String tenTaiSan,
        Integer soLuong,
        String moTa,
        String danhMucId
    ) {
        public ProductCreateRequest {
            if (tenTaiSan == null || tenTaiSan.isBlank()) {
                throw new IllegalArgumentException("tenTaiSan is required");
            }
            if (soLuong == null || soLuong <= 0) {
                throw new IllegalArgumentException("soLuong must be greater than 0");
            }
            if (danhMucId == null || danhMucId.isBlank()) {
                throw new IllegalArgumentException("danhMucId is required");
            }
        }
    }

    public record AuctionSessionResponse(
        String id,
        String image_src,
        String title,
        long current_bid,
        int bids,
        long time_left,
        String href
    ) {
    }
}