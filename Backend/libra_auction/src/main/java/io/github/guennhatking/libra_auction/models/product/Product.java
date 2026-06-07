package io.github.guennhatking.libra_auction.models.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.product.ProductStatus;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.person.Customer;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Customer creator;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    private java.util.List<Auction> auctions;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product")
    private List<ProductAttribute> attributes;

    @OneToMany(mappedBy = "product")
    private List<AttributeCombination> attributeCombinations;

    public List<AttributeCombination> getAttributeCombinations() {
        return attributeCombinations;
    }

    public void setAttributeCombinations(List<AttributeCombination> attributeCombinations) {
        this.attributeCombinations = attributeCombinations;
    }

    private String name;
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;

    // CONSTRUCTOR
    public Product() {
    }

    public Product(String name, int quantity, String description, Category category) {
        this.name = name;
        this.quantity = quantity;
        this.description = description;
        this.category = category;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public Customer getCreator() {
        return creator;
    }

    public java.util.List<Auction> getAuctions() {
        return auctions;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public List<ProductAttribute> getAttributes() {
        return attributes;
    }

    public ProductStatus getStatus() {
        return status;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setCreator(Customer creator) {
        this.creator = creator;
    }

    public void setAuctions(java.util.List<Auction> auctions) {
        this.auctions = auctions;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public void setAttributes(List<ProductAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
