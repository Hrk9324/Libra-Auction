package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.DanhMuc;
import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import io.github.guennhatking.libra_auction.models.TaiSan;
import io.github.guennhatking.libra_auction.models.ThongTinPhienDauGia;
import io.github.guennhatking.libra_auction.models.ThuocTinhTaiSan;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.repositories.DanhMucRepository;
import io.github.guennhatking.libra_auction.repositories.HinhAnhTaiSanRepository;
import io.github.guennhatking.libra_auction.repositories.TaiSanRepository;
import io.github.guennhatking.libra_auction.repositories.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final DanhMucRepository danhMucRepository;
    private final TaiSanRepository taiSanRepository;
    private final HinhAnhTaiSanRepository hinhAnhTaiSanRepository;
    private final PhienDauGiaRepository phienDauGiaRepository;

    public ProductService(DanhMucRepository danhMucRepository,
                          TaiSanRepository taiSanRepository,
                          HinhAnhTaiSanRepository hinhAnhTaiSanRepository,
                          PhienDauGiaRepository phienDauGiaRepository) {
        this.danhMucRepository = danhMucRepository;
        this.taiSanRepository = taiSanRepository;
        this.hinhAnhTaiSanRepository = hinhAnhTaiSanRepository;
        this.phienDauGiaRepository = phienDauGiaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return taiSanRepository.findAll().stream()
            .map(this::toProductResponse)
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
        ProductResponse response = new ProductResponse();
        
        // Product info
        response.setProduct_id(product.getId());
        response.setProduct_name(product.getTenTaiSan());
        response.setQuantity(product.getSoLuong());
        response.setDescription(product.getMoTa());
        
        // Category info
        if (product.getDanhMuc() != null) {
            response.setCategory_id(product.getDanhMuc().getId());
            response.setCategory_name(product.getDanhMuc().getTenDanhMuc());
        }
        
        // Auction info - get first/active auction session for this product
        List<PhienDauGia> auctionSessions = phienDauGiaRepository.findByTaiSan(product);
        if (auctionSessions != null && !auctionSessions.isEmpty()) {
            PhienDauGia auctionSession = auctionSessions.get(0); // Get first auction session
            response.setAuction_id(auctionSession.getId());
            response.setAuction_name(product.getTenTaiSan());
            response.setAuction_status(auctionSession.getTrangThaiPhien() != null ? auctionSession.getTrangThaiPhien().toString() : "UPCOMING");
            response.setAuction_type(auctionSession.getLoaiDauGia() != null ? auctionSession.getLoaiDauGia().toString() : "DAU_GIA_LEN");
            response.setStart_time(auctionSession.getThoiGianBatDau());
            response.setDuration(auctionSession.getThoiLuong());
            response.setStarting_price(auctionSession.getGiaKhoiDiem());
            response.setMin_bid_increment(auctionSession.getBuocGiaNhoNhat());
            response.setCurrent_price(auctionSession.getGiaKhoiDiem()); // Default to starting price
        }
        
        // Images
        if (product.getHinhAnhTaiSanList() != null && !product.getHinhAnhTaiSanList().isEmpty()) {
            List<String> imageUrls = product.getHinhAnhTaiSanList().stream()
                .map(HinhAnhTaiSan::getHinhAnh)
                .collect(Collectors.toList());
            response.setImages(imageUrls);
        }
        
        // Attributes
        if (product.getThuocTinhTaiSanList() != null && !product.getThuocTinhTaiSanList().isEmpty()) {
            List<ProductResponse.AttributeDTO> attributes = product.getThuocTinhTaiSanList().stream()
                .map(attr -> new ProductResponse.AttributeDTO(attr.getTenThuocTinh(), attr.getGiaTri()))
                .collect(Collectors.toList());
            response.setAttributes(attributes);
        }
        
        response.setTotal_bids(0);
        response.setTotal_participants(0);
        
        return response;
    }
}
