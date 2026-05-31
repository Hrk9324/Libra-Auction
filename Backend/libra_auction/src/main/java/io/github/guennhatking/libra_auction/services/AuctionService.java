package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.auction.ApprovalStatus;
import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.enums.product.ProductStatus;
import io.github.guennhatking.libra_auction.mappers.AuctionMapper;
import io.github.guennhatking.libra_auction.mappers.ProductResponseMapper;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.models.product.Product;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.repositories.product.ProductRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Service
public class AuctionService {
        private final AuctionRepository auctionRepository;
        private final AuctionMapper auctionMapper;
        private final ProductResponseMapper productResponseMapper;
        private final ProductRepository productRepository;
        private final CustomerRepository customerRepository;
        private final AuctionStateRedisService auctionStateRedisService;

        public AuctionService(AuctionRepository auctionRepository,
                        AuctionMapper auctionMapper,
                        ProductResponseMapper productResponseMapper,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        AuctionStateRedisService auctionStateRedisService) {
                this.auctionRepository = auctionRepository;
                this.auctionMapper = auctionMapper;
                this.productResponseMapper = productResponseMapper;
                this.productRepository = productRepository;
                this.customerRepository = customerRepository;
                this.auctionStateRedisService = auctionStateRedisService;
        }

        @Transactional(readOnly = true)
        public List<AuctionResponse> getAuctions() {
                List<Auction> auctionList = auctionRepository.findAll().stream()
                                .sorted(Comparator.comparing(Auction::getCreatedAt,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .toList();
                return auctionMapper.toAuctionResponseList(auctionList);
        }

        @Transactional(readOnly = true)
        public AuctionResponse getAuctionById(String id) {
                Auction session = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));
                // For public endpoint, only return approved auctions
                if (session.getApprovalStatus() != ApprovalStatus.APPROVED) {
                        throw new IllegalArgumentException("Auction session not found");
                }
                return auctionMapper.toAuctionResponse(session);
        }

        @Transactional(readOnly = true)
        public AuctionResponse getAuctionById(String id, String userId) {
                Auction session = auctionRepository.findByIdAndCreator_Id(id, userId)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                return auctionMapper.toAuctionResponse(session);
        }

        @Transactional(readOnly = true)
        public AuctionResponse getAuctionByIdAndCategory(String id, String categoryId) {
                Auction session = auctionRepository
                                .findByIdAndProduct_Category_Id(id, categoryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Auction not found in this category"));
                // For public endpoint, only return approved auctions
                if (session.getApprovalStatus() != ApprovalStatus.APPROVED) {
                        throw new IllegalArgumentException("Auction not found in this category");
                }
                return auctionMapper.toAuctionResponse(session);
        }

        @Transactional
        public AuctionResponse createAuction(AuctionCreateRequest request, String userId) {
                Product product = productRepository.findById(request.productId())
                                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

                if (product.getStatus() != ProductStatus.AVAILABLE) {
                        throw new IllegalArgumentException("Sản phẩm không ở trạng thái sẵn sàng. Trạng thái hiện tại: " + product.getStatus());
                }

                Customer creator = customerRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Auction session = new Auction();
                session.setCreatedAt(OffsetDateTime.now(ZoneOffset.ofHours(7)));
                session.setCreator(creator);
                session.setProduct(product);
                session.setDuration(request.duration());
                session.setStartTime(request.startTime());
                session.setStartingPrice(request.startingPrice());
                session.setMinimumBidIncrement(request.minimumBidIncrement());
                session.setDepositAmount(request.depositAmount());
                session.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
                session.setAuctionStatus(AuctionStatus.NOT_STARTED);


                Auction savedSession = auctionRepository.save(session);

                return auctionMapper.toAuctionResponse(savedSession);
        }

        @Transactional
        public AuctionResponse updateAuction(String id, AuctionUpdateRequest request, String userId) {
                Customer creator = customerRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Auction session = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (!creator.getId().equals(session.getCreator().getId())) {
                        throw new AccessDeniedException("You do not have permission to edit this auction.");
                }

                session.setStartTime(request.startTime());
                session.setDuration(request.duration());
                session.setStartingPrice(request.startingPrice());
                session.setMinimumBidIncrement(request.minimumBidIncrement());
                session.setDepositAmount(request.depositAmount());

                Auction updatedSession = auctionRepository.save(session);

                // If auction is already approved, update Redis scheduling
                if (updatedSession.getApprovalStatus() == ApprovalStatus.APPROVED) {
                        auctionStateRedisService.removeAuctionStartEvent(id);
                        auctionStateRedisService.removeAuctionEndEvent(id);

                        if (updatedSession.getStartTime() != null) {
                                auctionStateRedisService.addAuctionStartEvent(updatedSession.getId(), updatedSession.getStartTime());
                                auctionStateRedisService.addAuctionEndEvent(updatedSession.getId(),
                                        updatedSession.getStartTime().plusSeconds(updatedSession.getDuration()));
                        }
                }

                return auctionMapper.toAuctionResponse(updatedSession);
        }

        @Transactional
        public void deleteAuction(String id, String userId) {
                Customer creator = customerRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Auction session = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (!creator.getId().equals(session.getCreator().getId())) {
                        throw new AccessDeniedException("You do not have permission to delete this auction");
                }

                // Clean up Redis scheduling when deleting
                auctionStateRedisService.removeAuctionStartEvent(id);
                auctionStateRedisService.removeAuctionEndEvent(id);

                auctionRepository.delete(session);
        }

        // ========== ADMIN APPROVAL METHODS ==========

        @Transactional
        public AuctionResponse approveAuction(String id, String adminId) {
                Auction session = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (session.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
                        throw new IllegalStateException("Auction is not pending approval");
                }

                session.setApprovalStatus(ApprovalStatus.APPROVED);
                Auction saved = auctionRepository.save(session);

                // Register the auction in Redis for automatic start/end scheduling
                if (saved.getStartTime() != null) {
                        auctionStateRedisService.addAuctionStartEvent(saved.getId(), saved.getStartTime());
                        // Calculate end time: start_time + duration
                        auctionStateRedisService.addAuctionEndEvent(saved.getId(),
                                saved.getStartTime().plusSeconds(saved.getDuration()));
                }

                return auctionMapper.toAuctionResponse(saved);
        }

        @Transactional
        public AuctionResponse rejectAuction(String id, String adminId, String reason) {
                Auction session = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));

                if (session.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
                        throw new IllegalStateException("Auction is not pending approval");
                }

                session.setApprovalStatus(ApprovalStatus.REJECTED);
                Auction saved = auctionRepository.save(session);

                // Clean up Redis scheduling when rejecting
                auctionStateRedisService.removeAuctionStartEvent(id);
                auctionStateRedisService.removeAuctionEndEvent(id);

                return auctionMapper.toAuctionResponse(saved);
        }

        @Transactional
        public AuctionResponse completeAuction(String id, String adminId) {
                Auction auction = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                if (auction.getAuctionStatus() != AuctionStatus.ENDED) {
                        throw new IllegalStateException("Auction must be ENDED to complete");
                }

                // Mark product as SOLD
                Product product = auction.getProduct();
                if (product != null) {
                        product.setStatus(ProductStatus.SOLD);
                        productRepository.save(product);
                }

                auction.setAuctionStatus(AuctionStatus.COMPLETED);
                auction.setCompletedAt(OffsetDateTime.now(ZoneOffset.ofHours(7)));
                Auction saved = auctionRepository.save(auction);

                // Clean up Redis
                auctionStateRedisService.removeAuctionStartEvent(id);
                auctionStateRedisService.removeAuctionEndEvent(id);

                return auctionMapper.toAuctionResponse(saved);
        }

        @Transactional
        public AuctionResponse failAuction(String id, String adminId, String reason) {
                Auction auction = auctionRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                if (auction.getAuctionStatus() != AuctionStatus.ENDED) {
                        throw new IllegalStateException("Auction must be ENDED to mark as failed");
                }

                // Keep product as AVAILABLE
                Product product = auction.getProduct();
                if (product != null) {
                        product.setStatus(ProductStatus.AVAILABLE);
                        productRepository.save(product);
                }

                auction.setAuctionStatus(AuctionStatus.FAILED);
                auction.setFailureReason(reason);
                Auction saved = auctionRepository.save(auction);

                // Clean up Redis
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
                List<Auction> approvedNotStarted = auctionRepository.findByAuctionStatus(AuctionStatus.NOT_STARTED);
                int count = 0;

                for (Auction auction : approvedNotStarted) {
                        if (auction.getApprovalStatus() == ApprovalStatus.APPROVED &&
                            auction.getStartTime() != null) {
                                try {
                                        auctionStateRedisService.addAuctionStartEvent(auction.getId(), auction.getStartTime());
                                        auctionStateRedisService.addAuctionEndEvent(auction.getId(),
                                                auction.getStartTime().plusSeconds(auction.getDuration()));
                                        count++;
                                } catch (Exception e) {
                                        // Log and continue with next auction
                                }
                        }
                }

                return count;
        }

        // ========== PUBLIC PRODUCT RETRIEVAL METHODS ==========
        // Get product from an approved auction to ensure security

        @Transactional(readOnly = true)
        public ProductResponse getProductFromApprovedAuction(
                        String auctionId, String productId) {
                Auction session = auctionRepository.findById(auctionId)
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                // Verify auction is approved
                if (session.getApprovalStatus() != ApprovalStatus.APPROVED) {
                        throw new IllegalArgumentException("Auction is not approved");
                }

                Product product = session.getProduct();
                if (product == null) {
                        throw new IllegalArgumentException("Product not found in this auction");
                }

                // Verify it's the correct product
                if (!product.getId().equals(productId)) {
                        throw new IllegalArgumentException("Product does not belong to this auction");
                }

                // Return product response
                return productResponseMapper.toProductResponse(product);
        }
}
