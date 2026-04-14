package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {
    private String product_id;
    private String product_name;
    private String category_id;
    private String category_name;
    private Integer quantity;
    private String description;
    
    private String auction_id;
    private String auction_name;
    private String auction_status;
    private String auction_type;
    private LocalDateTime start_time;
    private Long duration;
    
    private Long starting_price;
    private Long current_price;
    private Long min_bid_increment;
    
    private List<String> images;
    private List<AttributeDTO> attributes;
    
    private Integer total_bids;
    private Integer total_participants;

    // Nested class for attributes
    public static class AttributeDTO {
        public String name;
        public String value;

        public AttributeDTO() {
        }

        public AttributeDTO(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    // Constructor
    public ProductResponse() {
    }

    // Getters and Setters
    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuction_id() {
        return auction_id;
    }

    public void setAuction_id(String auction_id) {
        this.auction_id = auction_id;
    }

    public String getAuction_name() {
        return auction_name;
    }

    public void setAuction_name(String auction_name) {
        this.auction_name = auction_name;
    }

    public String getAuction_status() {
        return auction_status;
    }

    public void setAuction_status(String auction_status) {
        this.auction_status = auction_status;
    }

    public String getAuction_type() {
        return auction_type;
    }

    public void setAuction_type(String auction_type) {
        this.auction_type = auction_type;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getStarting_price() {
        return starting_price;
    }

    public void setStarting_price(Long starting_price) {
        this.starting_price = starting_price;
    }

    public Long getCurrent_price() {
        return current_price;
    }

    public void setCurrent_price(Long current_price) {
        this.current_price = current_price;
    }

    public Long getMin_bid_increment() {
        return min_bid_increment;
    }

    public void setMin_bid_increment(Long min_bid_increment) {
        this.min_bid_increment = min_bid_increment;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<AttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public Integer getTotal_bids() {
        return total_bids;
    }

    public void setTotal_bids(Integer total_bids) {
        this.total_bids = total_bids;
    }

    public Integer getTotal_participants() {
        return total_participants;
    }

    public void setTotal_participants(Integer total_participants) {
        this.total_participants = total_participants;
    }
}
