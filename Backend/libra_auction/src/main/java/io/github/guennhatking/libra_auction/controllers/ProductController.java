package io.github.guennhatking.libra_auction.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.github.guennhatking.libra_auction.dto.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.dto.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.ProductResponse;
import io.github.guennhatking.libra_auction.service.ProductService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class ProductController {
    ProductService productService;

    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Creating new product: {}", request.getTenTaiSan());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get products by category
     * GET /api/products/category/{categoryId}
     * MUST come before /{id} to avoid routing conflicts
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryId) {
        log.info("Fetching products for category: {}", categoryId);
        List<ProductResponse> response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Fetching all products");
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        log.info("Fetching product with id: {}", id);
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update product by ID
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Updating product with id: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product by ID
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("Deleting product with id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
