package io.github.guennhatking.libra_auction.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.guennhatking.libra_auction.mappers.AuctionRegistrationMapper;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.auction.ThongTinThamGiaDauGia;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.repositories.auction.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.auction.ThongTinThamGiaDauGiaRepository;
import io.github.guennhatking.libra_auction.repositories.person.NguoiDungRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionRegistrationCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionRegistrationResponse;

@Service
public class AuctionRegistrationService {
        private final ThongTinThamGiaDauGiaRepository thongTinThamGiaDauGiaRepository;
        private final NguoiDungRepository nguoiDungRepository;
        private final PhienDauGiaRepository phienDauGiaRepository;
        private final AuctionRegistrationMapper auctionRegistrationMapper;

        public AuctionRegistrationService(ThongTinThamGiaDauGiaRepository thongTinThamGiaDauGiaRepository,
                        NguoiDungRepository nguoiDungRepository,
                        PhienDauGiaRepository phienDauGiaRepository,
                        AuctionRegistrationMapper auctionRegistrationMapper) {
                this.thongTinThamGiaDauGiaRepository = thongTinThamGiaDauGiaRepository;
                this.nguoiDungRepository = nguoiDungRepository;
                this.phienDauGiaRepository = phienDauGiaRepository;
                this.auctionRegistrationMapper = auctionRegistrationMapper;
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getAllRegistrations() {
                List<ThongTinThamGiaDauGia> entities = thongTinThamGiaDauGiaRepository.findAll();
                List<AuctionRegistrationResponse> responses = auctionRegistrationMapper.toResponseList(entities);
                return responses;
        }

        @Transactional(readOnly = true)
        public AuctionRegistrationResponse getRegistrationById(String id) {
                // 1. Tìm entity từ Database
                ThongTinThamGiaDauGia registration = thongTinThamGiaDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

                // 2. Sử dụng mapper để chuyển đổi và trả về
                return auctionRegistrationMapper.toResponse(registration);
        }

        @Transactional
        public AuctionRegistrationResponse registerForAuction(AuctionRegistrationCreateRequest request, String userId) {
                // 1. Kiểm tra User
                NguoiDung user = nguoiDungRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // 2. Kiểm tra Phiên đấu giá
                PhienDauGia auction = phienDauGiaRepository.findById(request.auctionId())
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                // 3. Kiểm tra đăng ký trùng lặp (Logic cũ giữ nguyên)
                boolean alreadyRegistered = Optional.ofNullable(auction.getDanhSachThamGia())
                                .orElse(Collections.emptyList())
                                .stream()
                                .anyMatch(reg -> reg.getNguoiThamGia().getId().equals(userId));

                if (alreadyRegistered) {
                        throw new IllegalArgumentException("User is already registered for this auction");
                }

                // 4. Tạo và lưu đăng ký
                ThongTinThamGiaDauGia registration = new ThongTinThamGiaDauGia(user, auction);
                ThongTinThamGiaDauGia savedRegistration = thongTinThamGiaDauGiaRepository.save(registration);

                return auctionRegistrationMapper.toResponse(savedRegistration);
        }

        @Transactional
        public void deleteRegistration(String id) {
                ThongTinThamGiaDauGia registration = thongTinThamGiaDauGiaRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
                thongTinThamGiaDauGiaRepository.delete(registration);
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getRegistrationsByUserId(String userId) {
                // 1. Kiểm tra User tồn tại
                if (!nguoiDungRepository.existsById(userId)) {
                        throw new IllegalArgumentException("User not found");
                }

                // 2. Lấy dữ liệu đã lọc từ DB (Thay vì lấy tất cả rồi filter)
                List<ThongTinThamGiaDauGia> entities = thongTinThamGiaDauGiaRepository.findByNguoiThamGiaId(userId);

                // 3. Sử dụng mapper để chuyển đổi toàn bộ danh sách
                return auctionRegistrationMapper.toResponseList(entities);
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getRegistrationsByAuctionId(String auctionId) {
                // 1. Kiểm tra phiên đấu giá có tồn tại không
                if (!phienDauGiaRepository.existsById(auctionId)) {
                        throw new IllegalArgumentException("Auction not found");
                }

                // 2. Lấy dữ liệu từ DB thông qua phương thức mới ở Repository
                List<ThongTinThamGiaDauGia> entities = thongTinThamGiaDauGiaRepository
                                .findByPhienDauGiaId(auctionId);

                // 3. Sử dụng Mapper để chuyển đổi sang danh sách Response
                return auctionRegistrationMapper.toResponseList(entities);
        }
}
