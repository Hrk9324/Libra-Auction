package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.DanhMuc;
import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.models.ThongTinPhienDauGia;
import io.github.guennhatking.libra_auction.repositories.DanhMucRepository;
import io.github.guennhatking.libra_auction.repositories.HinhAnhTaiSanRepository;
import io.github.guennhatking.libra_auction.repositories.TaiSanRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final DanhMucRepository danhMucRepository;
    private final TaiSanRepository taiSanRepository;
    private final HinhAnhTaiSanRepository hinhAnhTaiSanRepository;

    public ProductService(DanhMucRepository danhMucRepository,
                          TaiSanRepository taiSanRepository,
                          HinhAnhTaiSanRepository hinhAnhTaiSanRepository) {
        this.danhMucRepository = danhMucRepository;
        this.taiSanRepository = taiSanRepository;
        this.hinhAnhTaiSanRepository = hinhAnhTaiSanRepository;
    }

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

    @Transactional(readOnly = true)
    public ProductResponse getProductById(String id) {
        TaiSan product = taiSanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return toProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        DanhMuc category = danhMucRepository.findById(request.danhMucId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        TaiSan product = new TaiSan(
            request.tenTaiSan(),
            request.soLuong(),
            request.moTa(),
            category
        );

        TaiSan savedProduct = taiSanRepository.save(product);

        // Thêm hình ảnh từ Cloudinary
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.imageUrls()) {
                HinhAnhTaiSan image = new HinhAnhTaiSan(savedProduct, order++, imageUrl);
                hinhAnhTaiSanRepository.save(image);
            }
        }

        return toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        TaiSan product = taiSanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        DanhMuc category = danhMucRepository.findById(request.danhMucId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        product.setTenTaiSan(request.tenTaiSan());
        product.setSoLuong(request.soLuong());
        product.setMoTa(request.moTa());
        product.setDanhMuc(category);

        TaiSan updatedProduct = taiSanRepository.save(product);

        // Cập nhật hình ảnh từ Cloudinary
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            // Xóa hình ảnh cũ
            hinhAnhTaiSanRepository.findByTaiSanIdOrderByThuTuHienThiAsc(id)
                .forEach(hinhAnhTaiSanRepository::delete);

            // Thêm hình ảnh mới
            int order = 0;
            for (String imageUrl : request.imageUrls()) {
                HinhAnhTaiSan image = new HinhAnhTaiSan(updatedProduct, order++, imageUrl);
                hinhAnhTaiSanRepository.save(image);
            }
        }

        return toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(String id) {
        TaiSan product = taiSanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        taiSanRepository.delete(product);
    }

    private ProductResponse toProductResponse(TaiSan product) {
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
    }

    private String resolveProductImage(TaiSan product) {
        Optional<HinhAnhTaiSan> firstImage = hinhAnhTaiSanRepository.findByTaiSanIdOrderByThuTuHienThiAsc(product.getId()).stream().findFirst();
        return firstImage.map(HinhAnhTaiSan::getHinhAnh)
            .orElseGet(() -> product.getDanhMuc() != null ? product.getDanhMuc().getHinhAnh() : null);
    }
}
