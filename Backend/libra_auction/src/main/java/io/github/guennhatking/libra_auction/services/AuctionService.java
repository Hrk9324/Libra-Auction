package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.auction.TrangThaiKiemDuyet;
import io.github.guennhatking.libra_auction.enums.auction.TrangThaiPhien;
import io.github.guennhatking.libra_auction.mappers.AuctionMapper;
import io.github.guennhatking.libra_auction.mappers.ProductResponseMapper;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.models.product.TaiSan;
import io.github.guennhatking.libra_auction.repositories.auction.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.person.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repositories.product.TaiSanRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
public class AuctionService {
        private final PhienDauGiaRepository phienDauGiaRepository;
        private final AuctionMapper auctionMapper;
        private final ProductResponseMapper productResponseMapper;
        private final TaiSanRepository taiSanRepository;
        private final NguoiDungRepository nguoiDungRepository;
        private final AuctionStateRedisService auctionStateRedisService;

        public AuctionService(PhienDauGiaRepository phienDauGiaRepository,
                        AuctionMapper auctionMapper,
                        ProductResponseMapper productResponseMapper,
                        TaiSanRepository taiSanRepository,
                        NguoiDungRepository nguoiDungRepository,
                        AuctionStateRedisService auctionStateRedisService) {
                this.phienDauGiaRepository = phienDauGiaRepository;
                this.auctionMapper = auctionMapper;
                this.productResponseMapper = productResponseMapper;
                this.taiSanRepository = taiSanRepository;
                this.nguoiDungRepository = nguoiDungRepository;
                this.auctionStateRedisService = auctionStateRedisService;
        }

        @Transactional(readOnly = true)
        public List<AuctionResponse> getAuctions() {
                List<PhienDauGia> phienDauGiaList = phienDauGiaRepository.findAll().stream()
                                .sorted(Comparator.comparing(PhienDauGia::getThoiGianTao,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .toList();
                return auctionMapper.toAuctionResponseList(phienDauGiaList);
        }

        @Transactional(readOnly = true)
        public AuctionResponse getAuctionById(String id) {
                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));
                // For public endpoint, only return approved auctions
                if (session.getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Auction session not found");
                }
                // Also check if the product is approved
                if (session.getTaiSan() == null || 
                    session.getTaiSan().getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Auction session not found");
                }
                return auctionMapper.toAuctionResponse(session);
        }

        @Transactional(readOnly = true)
        public AuctionResponse getAuctionByIdAndCategory(String id, String categoryId) {
                PhienDauGia session = phienDauGiaRepository
                                .findByIdAndTaiSan_DanhMuc_Id(id, categoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Auction not found in this category"));
                // For public endpoint, only return approved auctions
                if (session.getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Auction not found in this category");
                }
                // Also check if the product is approved
                if (session.getTaiSan() == null || 
                    session.getTaiSan().getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Auction not found in this category");
                }
                return auctionMapper.toAuctionResponse(session);
        }

        @Transactional
        public AuctionResponse createAuction(AuctionCreateRequest request, String userId) {
                TaiSan product = taiSanRepository.findById(request.taiSanId())
                                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

                NguoiDung nguoiTao = nguoiDungRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                PhienDauGia session = new PhienDauGia();
                session.setNguoiTao(nguoiTao);
                session.setTaiSan(product);
                session.setThoiLuong(request.thoiLuong());
                session.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.CHUA_DUYET);
                session.setTrangThaiPhien(TrangThaiPhien.CHUA_BAT_DAU);


                PhienDauGia savedSession = phienDauGiaRepository.save(session);

                return auctionMapper.toAuctionResponse(savedSession);
        }

        @Transactional
        public AuctionResponse updateAuction(String id, AuctionUpdateRequest request, String userId) {
                NguoiDung nguoiTao = nguoiDungRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (!nguoiTao.getId().equals(session.getNguoiTao().getId())) {
                        throw new AccessDeniedException("You do not have permission to edit this auction.");
                }

                session.setThoiGianBatDau(request.thoiGianBatDau());
                session.setThoiLuong(request.thoiLuong());
                session.setGiaKhoiDiem(request.giaKhoiDiem());
                session.setBuocGiaNhoNhat(request.buocGiaNhoNhat());
                session.setTienCoc(request.tienCoc());

                PhienDauGia updatedSession = phienDauGiaRepository.save(session);

                // If auction is already approved, update Redis scheduling
                if (updatedSession.getTrangThaiKiemDuyet() == TrangThaiKiemDuyet.DA_DUYET) {
                        auctionStateRedisService.removeAuctionStartEvent(id);
                        auctionStateRedisService.removeAuctionEndEvent(id);
                        
                        if (updatedSession.getThoiGianBatDau() != null) {
                                auctionStateRedisService.addAuctionStartEvent(updatedSession.getId(), updatedSession.getThoiGianBatDau());
                                auctionStateRedisService.addAuctionEndEvent(updatedSession.getId(), 
                                        updatedSession.getThoiGianBatDau().plusSeconds(updatedSession.getThoiLuong()));
                        }
                }

                return auctionMapper.toAuctionResponse(updatedSession);
        }

        @Transactional
        public void deleteAuction(String id, String userId) {
                NguoiDung nguoiTao = nguoiDungRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (!nguoiTao.getId().equals(session.getNguoiTao().getId())) {
                        throw new AccessDeniedException("Bạn không có quyền xóa phiên đấu giá này");
                }

                // Clean up Redis scheduling when deleting
                auctionStateRedisService.removeAuctionStartEvent(id);
                auctionStateRedisService.removeAuctionEndEvent(id);

                phienDauGiaRepository.delete(session);
        }

        // ========== ADMIN APPROVAL METHODS ==========

        @Transactional
        public AuctionResponse approveAuction(String id, String adminId) {
                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                session.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.DA_DUYET);
                PhienDauGia saved = phienDauGiaRepository.save(session);

                // Register the auction in Redis for automatic start/end scheduling
                if (saved.getThoiGianBatDau() != null) {
                        auctionStateRedisService.addAuctionStartEvent(saved.getId(), saved.getThoiGianBatDau());
                        // Calculate end time: start_time + duration
                        auctionStateRedisService.addAuctionEndEvent(saved.getId(), 
                                saved.getThoiGianBatDau().plusSeconds(saved.getThoiLuong()));
                }

                return auctionMapper.toAuctionResponse(saved);
        }

        @Transactional
        public AuctionResponse rejectAuction(String id, String adminId, String reason) {
                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                session.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.BI_TU_CHOI);
                PhienDauGia saved = phienDauGiaRepository.save(session);

                // Clean up Redis scheduling when rejecting
                auctionStateRedisService.removeAuctionStartEvent(id);
                auctionStateRedisService.removeAuctionEndEvent(id);

                return auctionMapper.toAuctionResponse(saved);
        }

        /**
         * Register all existing approved but not-yet-started auctions in Redis.
         * This is useful after deploying the fix to retroactively register auctions
         * that were approved before Redis integration was added.
         * @return Number of auctions registered
         */
        public int registerExistingAuctionsToRedis() {
                List<PhienDauGia> approvedNotStarted = phienDauGiaRepository.findByTrangThaiPhien(TrangThaiPhien.CHUA_BAT_DAU);
                int count = 0;
                
                for (PhienDauGia auction : approvedNotStarted) {
                        if (auction.getTrangThaiKiemDuyet() == TrangThaiKiemDuyet.DA_DUYET && 
                            auction.getThoiGianBatDau() != null) {
                                try {
                                        auctionStateRedisService.addAuctionStartEvent(auction.getId(), auction.getThoiGianBatDau());
                                        auctionStateRedisService.addAuctionEndEvent(auction.getId(), 
                                                auction.getThoiGianBatDau().plusSeconds(auction.getThoiLuong()));
                                        count++;
                                } catch (Exception e) {
                                        // Log and continue with next auction
                                }
                        }
                }
                
                return count;
        }

        // ========== MERGED ADMIN APPROVAL METHODS ==========
        // Approves both auction and its related product

        @Transactional
        public AuctionResponse approveAuctionWithProduct(String id, String adminId) {
                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                // Approve the auction
                session.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.DA_DUYET);
                PhienDauGia saved = phienDauGiaRepository.save(session);

                // Also approve the related product
                TaiSan product = saved.getTaiSan();
                if (product != null) {
                        product.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.DA_DUYET);
                        taiSanRepository.save(product);
                }

                return auctionMapper.toAuctionResponse(saved);
        }

        @Transactional
        public AuctionResponse rejectAuctionWithProduct(String id, String adminId, String reason) {
                PhienDauGia session = phienDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                // Reject the auction
                session.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.BI_TU_CHOI);
                PhienDauGia saved = phienDauGiaRepository.save(session);

                // Also reject the related product
                TaiSan product = saved.getTaiSan();
                if (product != null) {
                        product.setTrangThaiKiemDuyet(TrangThaiKiemDuyet.BI_TU_CHOI);
                        taiSanRepository.save(product);
                }

                return auctionMapper.toAuctionResponse(saved);
        }

        // ========== PUBLIC PRODUCT RETRIEVAL METHODS ==========
        // Get product from an approved auction to ensure security

        @Transactional(readOnly = true)
        public ProductResponse getProductFromApprovedAuction(
                        String auctionId, String productId) {
                PhienDauGia session = phienDauGiaRepository.findById(auctionId)
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                // Verify auction is approved
                if (session.getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Auction is not approved");
                }

                TaiSan product = session.getTaiSan();
                if (product == null) {
                        throw new IllegalArgumentException("Product not found in this auction");
                }

                // Verify it's the correct product
                if (!product.getId().equals(productId)) {
                        throw new IllegalArgumentException("Product does not belong to this auction");
                }

                // Verify product is also approved
                if (product.getTrangThaiKiemDuyet() != TrangThaiKiemDuyet.DA_DUYET) {
                        throw new IllegalArgumentException("Product is not approved");
                }

                // Return product response
                return productResponseMapper.toProductResponse(product);
        }
}
