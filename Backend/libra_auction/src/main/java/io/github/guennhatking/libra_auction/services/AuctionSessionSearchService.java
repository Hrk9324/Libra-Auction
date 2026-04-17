package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.repositories.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSessionSearchRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionSessionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for searching auction sessions (Phiên Đấu Giá)
 * 
 * Business logic for filtering, sorting, and paginating auction sessions
 * Separates concerns from controller for maintainability
 */
@Service
public class AuctionSessionSearchService {
    private final PhienDauGiaRepository phienDauGiaRepository;

    public AuctionSessionSearchService(PhienDauGiaRepository phienDauGiaRepository) {
        this.phienDauGiaRepository = phienDauGiaRepository;
    }

    /**
     * Get all auction sessions matching search criteria
     * Tìm kiếm tất cả phiên đấu giá theo tiêu chí
     */
    public PageResponse<AuctionSessionResponse> searchAuctionSessions(AuctionSessionSearchRequest criteria) {
        // Get all sessions
        List<PhienDauGia> allSessions = phienDauGiaRepository.findAll();

        // Apply filters
        List<PhienDauGia> filtered = applyFilters(allSessions, criteria);

        // Apply sorting
        filtered = applySort(filtered, criteria);

        // Apply pagination
        return applyPagination(filtered, criteria);
    }

    /**
     * Get live auction sessions (DANG_DIEN_RA)
     * Lấy phiên đấu giá đang diễn ra
     */
    public PageResponse<AuctionSessionResponse> getLiveAuctionSessions(AuctionSessionSearchRequest criteria) {
        criteria.setStatus(Enums.TrangThaiPhien.DANG_DIEN_RA.toString());
        return searchAuctionSessions(criteria);
    }

    /**
     * Get upcoming auction sessions (CHUA_BAT_DAU)
     * Lấy phiên đấu giá sắp tới
     */
    public PageResponse<AuctionSessionResponse> getUpcomingAuctionSessions(AuctionSessionSearchRequest criteria) {
        criteria.setStatus(Enums.TrangThaiPhien.CHUA_BAT_DAU.toString());
        return searchAuctionSessions(criteria);
    }

    /**
     * Get auction sessions by category (có thể là live hoặc upcoming)
     * Lấy phiên đấu giá theo danh mục
     */
    public PageResponse<AuctionSessionResponse> getAuctionSessionsByCategory(
            String categoryId, String type, AuctionSessionSearchRequest criteria) {
        
        criteria.setCategoryId(categoryId);
        
        if ("live".equalsIgnoreCase(type)) {
            criteria.setStatus(Enums.TrangThaiPhien.DANG_DIEN_RA.toString());
        } else if ("upcoming".equalsIgnoreCase(type)) {
            criteria.setStatus(Enums.TrangThaiPhien.CHUA_BAT_DAU.toString());
        }
        
        return searchAuctionSessions(criteria);
    }

    /**
     * Apply all filters to sessions
     * - By product name
     * - By category
     * - By price range
     * - By time range
     * - By status
     * - By attributes
     */
    private List<PhienDauGia> applyFilters(List<PhienDauGia> sessions, AuctionSessionSearchRequest criteria) {
        return sessions.stream()
                .filter(session -> filterByName(session, criteria.getName()))
                .filter(session -> filterByCategory(session, criteria.getCategoryId()))
                .filter(session -> filterByPriceRange(session, criteria.getPriceFrom(), criteria.getPriceTo()))
                .filter(session -> filterByStartingPrice(session, criteria.getStartingPrice()))
                .filter(session -> filterByTimeRange(session, criteria.getTimeStart(), criteria.getTimeEnd()))
                .filter(session -> filterByStatus(session, criteria.getStatus()))
                .filter(session -> filterByAttributes(session, criteria.getAttributes()))
                .collect(Collectors.toList());
    }

    /**
     * Filter by product name (case-insensitive, partial match)
     */
    private boolean filterByName(PhienDauGia session, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        return session.getTaiSan() != null && 
               session.getTaiSan().getTenTaiSan() != null &&
               session.getTaiSan().getTenTaiSan().toLowerCase().contains(name.toLowerCase());
    }

    /**
     * Filter by category
     */
    private boolean filterByCategory(PhienDauGia session, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return true;
        }
        return session.getTaiSan() != null && 
               session.getTaiSan().getDanhMuc() != null &&
               session.getTaiSan().getDanhMuc().getId().equals(categoryId);
    }

    /**
     * Filter by current price range (giá hiện tại)
     */
    private boolean filterByPriceRange(PhienDauGia session, Long priceFrom, Long priceTo) {
        if (priceFrom == null && priceTo == null) {
            return true;
        }

        long currentPrice = session.getGiaHienTai() != 0 ? session.getGiaHienTai() : session.getGiaKhoiDiem();
        
        if (priceFrom != null && currentPrice < priceFrom) {
            return false;
        }
        if (priceTo != null && currentPrice > priceTo) {
            return false;
        }
        
        return true;
    }

    /**
     * Filter by starting price
     */
    private boolean filterByStartingPrice(PhienDauGia session, Long startingPrice) {
        if (startingPrice == null) {
            return true;
        }
        return session.getGiaKhoiDiem() == startingPrice;
    }

    /**
     * Filter by time range (auction start time)
     */
    private boolean filterByTimeRange(PhienDauGia session, LocalDateTime timeStart, LocalDateTime timeEnd) {
        if (timeStart == null && timeEnd == null) {
            return true;
        }

        LocalDateTime sessionTime = session.getThoiGianBatDau();
        
        if (timeStart != null && sessionTime.isBefore(timeStart)) {
            return false;
        }
        if (timeEnd != null && sessionTime.isAfter(timeEnd)) {
            return false;
        }
        
        return true;
    }

    /**
     * Filter by status (DANG_DIEN_RA, CHUA_BAT_DAU, etc)
     */
    private boolean filterByStatus(PhienDauGia session, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return session.getTrangThaiPhien() != null &&
               session.getTrangThaiPhien().toString().equals(status);
    }

    /**
     * Filter by attributes (attributes của tài sản)
     * Example: [{attribute_name: "Màu", attribute_value: "Đỏ"}]
     */
    private boolean filterByAttributes(PhienDauGia session, List<java.util.Map<String, String>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return true;
        }
        
        // Nếu session không có tài sản hoặc không có thuộc tính thì bỏ qua
        if (session.getTaiSan() == null || 
            session.getTaiSan().getThuocTinhTaiSanList() == null ||
            session.getTaiSan().getThuocTinhTaiSanList().isEmpty()) {
            return false;
        }

        // Check if all attribute filters match
        return attributes.stream().allMatch(filterAttr -> {
            String attrName = filterAttr.get("attribute_name");
            String attrValue = filterAttr.get("attribute_value");
            
            return session.getTaiSan().getThuocTinhTaiSanList().stream()
                    .anyMatch(taiSanAttr -> 
                        taiSanAttr.getTenThuocTinh() != null &&
                        taiSanAttr.getTenThuocTinh().equals(attrName) &&
                        taiSanAttr.getGiaTri() != null &&
                        taiSanAttr.getGiaTri().equals(attrValue)
                    );
        });
    }

    /**
     * Apply sorting based on criteria
     */
    private List<PhienDauGia> applySort(List<PhienDauGia> sessions, AuctionSessionSearchRequest criteria) {
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "thoiGianBatDau";
        boolean isAsc = "ASC".equalsIgnoreCase(criteria.getSortOrder());

        Comparator<PhienDauGia> comparator = switch(sortBy) {
            case "giaKhoiDiem" -> Comparator.comparing(PhienDauGia::getGiaKhoiDiem);
            case "giaHienTai" -> Comparator.comparing(PhienDauGia::getGiaHienTai);
            case "thoiGianBatDau" -> Comparator.comparing(PhienDauGia::getThoiGianBatDau);
            default -> Comparator.comparing(PhienDauGia::getThoiGianBatDau);
        };

        if (!isAsc) {
            comparator = comparator.reversed();
        }

        return sessions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Apply pagination to results
     */
    private PageResponse<AuctionSessionResponse> applyPagination(List<PhienDauGia> sessions, 
                                                                  AuctionSessionSearchRequest criteria) {
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int pageSize = criteria.getPageSize() != null ? criteria.getPageSize() : 20;
        
        int totalElements = sessions.size();
        int totalPages = (totalElements + pageSize - 1) / pageSize;
        
        int startIndex = Math.min(page * pageSize, totalElements);
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        List<PhienDauGia> pageContent = sessions.subList(startIndex, endIndex);
        List<AuctionSessionResponse> responseContent = pageContent.stream()
                .map(this::toAuctionSessionResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                responseContent,
                totalPages,
                (long) totalElements,
                page,
                pageSize,
                page == 0,
                page == totalPages - 1
        );
    }

    /**
     * Convert PhienDauGia entity to AuctionSessionResponse DTO
     */
    private AuctionSessionResponse toAuctionSessionResponse(PhienDauGia session) {
        AuctionSessionResponse response = new AuctionSessionResponse();
        var product = session.getTaiSan();
        
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
        }
        
        // Images
        if (product != null && product.getHinhAnhTaiSanList() != null && !product.getHinhAnhTaiSanList().isEmpty()) {
            List<String> images = product.getHinhAnhTaiSanList().stream()
                    .map(HinhAnhTaiSan::getHinhAnh)
                    .collect(Collectors.toList());
            response.setImages(images);
        }
        
        return response;
    }

    /**
     * Resolve auction title from session and product
     */
    private String resolveAuctionTitle(PhienDauGia session, io.github.guennhatking.libra_auction.models.TaiSan product) {
        if (session.getThongTinPhienDauGia() != null && session.getThongTinPhienDauGia().getTieuDe() != null) {
            return session.getThongTinPhienDauGia().getTieuDe();
        }
        if (product != null) {
            return product.getTenTaiSan();
        }
        return "Auction Session";
    }
}
