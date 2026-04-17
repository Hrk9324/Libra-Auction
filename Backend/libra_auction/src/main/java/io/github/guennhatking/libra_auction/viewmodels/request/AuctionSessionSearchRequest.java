package io.github.guennhatking.libra_auction.viewmodels.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request object for searching auction sessions (Phiên Đấu Giá)
 * 支持灵活搜索:
 * - By product name
 * - By category
 * - By price range
 * - By time range
 * - By attributes
 */
public class AuctionSessionSearchRequest {
    // Product search
    private String name;
    private String categoryId;

    // Price range
    private Long priceFrom;
    private Long priceTo;
    private Long startingPrice;

    // Time range
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;

    // Attributes filter
    private List<Map<String, String>> attributes;

    // Status filters
    private String status; // CHUA_BAT_DAU, DANG_DIEN_RA, DA_KET_THUC, BI_HUY
    
    // Pagination
    private Integer page = 0;
    private Integer pageSize = 20;
    
    // Sorting
    private String sortBy = "thoiGianBatDau"; // thoiGianBatDau, giaKhoiDiem, giaHienTai
    private String sortOrder = "DESC";

    // Constructor
    public AuctionSessionSearchRequest() {
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Long getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(Long priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Long getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(Long priceTo) {
        this.priceTo = priceTo;
    }

    public Long getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(Long startingPrice) {
        this.startingPrice = startingPrice;
    }

    public LocalDateTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalDateTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(LocalDateTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public List<Map<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Map<String, String>> attributes) {
        this.attributes = attributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
