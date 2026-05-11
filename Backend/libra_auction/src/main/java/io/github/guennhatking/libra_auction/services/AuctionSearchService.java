package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.mappers.AuctionMapper;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.repositories.auction.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSearchRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.enums.auction.TrangThaiKiemDuyet;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionSearchService {
    private final PhienDauGiaRepository phienDauGiaRepository;
    private final AuctionMapper auctionMapper;

    public AuctionSearchService(PhienDauGiaRepository phienDauGiaRepository,
            AuctionMapper auctionMapper) {
        this.phienDauGiaRepository = phienDauGiaRepository;
        this.auctionMapper = auctionMapper;
    }

    public PageResponse<AuctionResponse> searchAuctions(AuctionSearchRequest criteria) {
        // Get all sessions
        List<PhienDauGia> allSessions = phienDauGiaRepository.findAll();

        // Apply filters
        List<PhienDauGia> filtered = applyFilters(allSessions, criteria);

        // Apply sorting
        filtered = applySort(filtered, criteria);

        // Apply pagination
        return applyPagination(filtered, criteria);
    }

    public PageResponse<AuctionResponse> getLiveAuctions(AuctionSearchRequest criteria) {
        return searchAuctions(criteria);
    }

    public PageResponse<AuctionResponse> getUpcomingAuctions(AuctionSearchRequest criteria) {
        return searchAuctions(criteria);
    }

    public PageResponse<AuctionResponse> getAuctionsByCategory(AuctionSearchRequest criteria) {
        return searchAuctions(criteria);
    }

    /**
     * Search only approved auctions for public viewing
     */
    public PageResponse<AuctionResponse> searchPublicAuctions(AuctionSearchRequest baseCriteria) {
        // Force approval status to DA_DUYET for public
        AuctionSearchRequest criteria = new AuctionSearchRequest(
                baseCriteria.name(),
                baseCriteria.categoryId(),
                baseCriteria.priceFrom(),
                baseCriteria.priceTo(),
                baseCriteria.startingPrice(),
                baseCriteria.timeStart(),
                baseCriteria.timeEnd(),
                baseCriteria.attributes(),
                baseCriteria.status(),
                TrangThaiKiemDuyet.DA_DUYET.toString(),
                baseCriteria.page(),
                baseCriteria.pageSize(),
                baseCriteria.sortBy(),
                baseCriteria.sortOrder(),
                null);
        
        return searchAuctions(criteria);
    }

    /**
     * Search pending auctions (CHUA_DUYET) for admin
     */
    public PageResponse<AuctionResponse> searchPendingAuctions(AuctionSearchRequest baseCriteria) {
        // Force approval status to CHUA_DUYET for admin pending view
        AuctionSearchRequest criteria = new AuctionSearchRequest(
                baseCriteria.name(),
                baseCriteria.categoryId(),
                baseCriteria.priceFrom(),
                baseCriteria.priceTo(),
                baseCriteria.startingPrice(),
                baseCriteria.timeStart(),
                baseCriteria.timeEnd(),
                baseCriteria.attributes(),
                baseCriteria.status(),
                TrangThaiKiemDuyet.CHUA_DUYET.toString(),
                baseCriteria.page(),
                baseCriteria.pageSize(),
                baseCriteria.sortBy(),
                baseCriteria.sortOrder(),
                null);
        
        return searchAuctions(criteria);
    }

    private List<PhienDauGia> applyFilters(List<PhienDauGia> sessions, AuctionSearchRequest criteria) {
        return sessions.stream()
                .filter(session -> filterByName(session, criteria.name()))
                .filter(session -> filterByCategory(session, criteria.categoryId()))
                .filter(session -> filterByPriceRange(session, criteria.priceFrom(), criteria.priceTo()))
                .filter(session -> filterByStartingPrice(session, criteria.startingPrice()))
                .filter(session -> filterByTimeRange(session, criteria.timeStart(), criteria.timeEnd()))
                .filter(session -> filterByStatus(session, criteria.status()))
                .filter(session -> filterByOwner(session, criteria.chuSoHuuId()))
                .filter(session -> filterByApprovalStatus(session, criteria.trangThaiKiemDuyet(), criteria.chuSoHuuId()))
                .filter(session -> filterByProductApprovalStatus(session, criteria.trangThaiKiemDuyet(), criteria.chuSoHuuId()))
                .filter(session -> filterByAttributes(session, criteria.attributes()))
                .collect(Collectors.toList());
    }


    private boolean filterByName(PhienDauGia session, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        return session.getTaiSan() != null &&
                session.getTaiSan().getTenTaiSan() != null &&
                session.getTaiSan().getTenTaiSan().toLowerCase().contains(name.toLowerCase());
    }

    private boolean filterByCategory(PhienDauGia session, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return true;
        }
        return session.getTaiSan() != null &&
                session.getTaiSan().getDanhMuc() != null &&
                session.getTaiSan().getDanhMuc().getId().equals(categoryId);
    }

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

    private boolean filterByStartingPrice(PhienDauGia session, Long startingPrice) {
        if (startingPrice == null) {
            return true;
        }
        return session.getGiaKhoiDiem() == startingPrice;
    }

    private boolean filterByTimeRange(PhienDauGia session, OffsetDateTime timeStart, OffsetDateTime timeEnd) {
        if (timeStart == null && timeEnd == null) {
            return true;
        }

        OffsetDateTime sessionTime = session.getThoiGianBatDau();

        if (timeStart != null && sessionTime.isBefore(timeStart)) {
            return false;
        }
        if (timeEnd != null && sessionTime.isAfter(timeEnd)) {
            return false;
        }

        return true;
    }

    private boolean filterByStatus(PhienDauGia session, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return session.getTrangThaiPhien() != null &&
                session.getTrangThaiPhien().toString().equals(status);
    }

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
                    .anyMatch(taiSanAttr -> taiSanAttr.getTenThuocTinh() != null &&
                            taiSanAttr.getTenThuocTinh().equals(attrName) &&
                            taiSanAttr.getGiaTri() != null &&
                            taiSanAttr.getGiaTri().equals(attrValue));
        });
    }

    private boolean filterByOwner(PhienDauGia session, String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return true; // no owner filter
        }
        if (session.getNguoiTao() == null) {
            return false;
        }
        return ownerId.equals(session.getNguoiTao().getId());
    }

    private boolean filterByApprovalStatus(PhienDauGia session, String trangThaiKiemDuyet, String chuSoHuuId) {
        // If a specific approval status filter is specified (e.g., admin filtering), apply it
        if (trangThaiKiemDuyet != null && !trangThaiKiemDuyet.isBlank()) {
            if (session.getTrangThaiKiemDuyet() == null) {
                return false;
            }
            return session.getTrangThaiKiemDuyet().toString().equals(trangThaiKiemDuyet);
        }
        
        // If seller is viewing their own auctions (chuSoHuuId is set), show all their auctions
        // regardless of approval status
        if (chuSoHuuId != null && !chuSoHuuId.isBlank()) {
            return true;
        }
        
        // For public/unauthenticated users, only show approved auctions
        return session.getTrangThaiKiemDuyet() != null && 
               session.getTrangThaiKiemDuyet().equals(TrangThaiKiemDuyet.DA_DUYET);
    }

    private boolean filterByProductApprovalStatus(PhienDauGia session, String trangThaiKiemDuyet, String chuSoHuuId) {
        // If a specific approval status filter is specified (admin viewing), don't check product approval
        if (trangThaiKiemDuyet != null && !trangThaiKiemDuyet.isBlank()) {
            return true;
        }
        
        // If seller is viewing their own auctions, allow any product approval status
        if (chuSoHuuId != null && !chuSoHuuId.isBlank()) {
            return session.getTaiSan() != null;
        }
        
        // For public viewing, product must also be approved
        if (session.getTaiSan() == null) {
            return false;
        }
        return session.getTaiSan().getTrangThaiKiemDuyet() != null &&
               session.getTaiSan().getTrangThaiKiemDuyet().equals(TrangThaiKiemDuyet.DA_DUYET);
    }

    private List<PhienDauGia> applySort(List<PhienDauGia> sessions, AuctionSearchRequest criteria) {
        String sortBy = criteria.sortBy() != null ? criteria.sortBy() : "thoiGianBatDau";
        boolean isAsc = "ASC".equalsIgnoreCase(criteria.sortOrder());

        Comparator<PhienDauGia> comparator = switch (sortBy) {
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

    private PageResponse<AuctionResponse> applyPagination(List<PhienDauGia> sessions,
            AuctionSearchRequest criteria) {
        int page = criteria.page() != null ? criteria.page() : 0;
        int pageSize = criteria.pageSize() != null ? criteria.pageSize() : 20;

        int totalElements = sessions.size();
        int totalPages = (totalElements + pageSize - 1) / pageSize;

        int startIndex = Math.min(page * pageSize, totalElements);
        int endIndex = Math.min(startIndex + pageSize, totalElements);

        List<PhienDauGia> pageContent = sessions.subList(startIndex, endIndex);
        List<AuctionResponse> responseContent = auctionMapper.toAuctionResponseList(pageContent);

        return new PageResponse<>(
                responseContent,
                totalPages,
                (long) totalElements,
                page,
                pageSize,
                page == 0,
                page == totalPages - 1);
    }
}
