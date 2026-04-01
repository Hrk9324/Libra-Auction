package io.github.guennhatking.libra_auction.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.guennhatking.libra_auction.dto.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.dto.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.ProductResponse;
import io.github.guennhatking.libra_auction.exception.AppException;
import io.github.guennhatking.libra_auction.exception.ErrorCode;
import io.github.guennhatking.libra_auction.mapper.ProductMapper;
import io.github.guennhatking.libra_auction.models.DanhMuc;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.repos.DanhMucRepository;
import io.github.guennhatking.libra_auction.repos.TaiSanRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {
    TaiSanRepository taiSanRepository;
    DanhMucRepository danhMucRepository;
    ProductMapper productMapper;

    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product: {}", request.getTenTaiSan());

        TaiSan taiSan = productMapper.toProduct(request);

        if (request.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(request.getDanhMucId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            taiSan.setDanhMuc(danhMuc);
        }

        TaiSan savedProduct = taiSanRepository.save(taiSan);
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse getProduct(String id) {
        log.info("Getting product with id: {}", id);

        TaiSan taiSan = taiSanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return productMapper.toProductResponse(taiSan);
    }

    public List<ProductResponse> getAllProducts() {
        log.info("Getting all products");
        return taiSanRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        log.info("Updating product with id: {}", id);

        TaiSan taiSan = taiSanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getTenTaiSan() != null) {
            taiSan.setTenTaiSan(request.getTenTaiSan());
        }
        if (request.getSoLuong() > 0) {
            taiSan.setSoLuong(request.getSoLuong());
        }
        if (request.getMoTa() != null) {
            taiSan.setMoTa(request.getMoTa());
        }
        if (request.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(request.getDanhMucId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            taiSan.setDanhMuc(danhMuc);
        }

        TaiSan updatedProduct = taiSanRepository.save(taiSan);
        return productMapper.toProductResponse(updatedProduct);
    }

    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);

        TaiSan taiSan = taiSanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        taiSanRepository.delete(taiSan);
    }

    public List<ProductResponse> getProductsByCategory(String categoryId) {
        log.info("Getting products by category: {}", categoryId);

        DanhMuc danhMuc = danhMucRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return taiSanRepository.findByDanhMuc(danhMuc).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }
}
