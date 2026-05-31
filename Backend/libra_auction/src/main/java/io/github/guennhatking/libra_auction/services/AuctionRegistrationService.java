package io.github.guennhatking.libra_auction.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.guennhatking.libra_auction.mappers.AuctionRegistrationMapper;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.auction.AuctionParticipationInfo;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionParticipationInfoRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionRegistrationCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionRegistrationResponse;

@Service
public class AuctionRegistrationService {
        private final AuctionParticipationInfoRepository participationInfoRepository;
        private final CustomerRepository customerRepository;
        private final AuctionRepository auctionRepository;
        private final AuctionRegistrationMapper auctionRegistrationMapper;

        public AuctionRegistrationService(AuctionParticipationInfoRepository participationInfoRepository,
                        CustomerRepository customerRepository,
                        AuctionRepository auctionRepository,
                        AuctionRegistrationMapper auctionRegistrationMapper) {
                this.participationInfoRepository = participationInfoRepository;
                this.customerRepository = customerRepository;
                this.auctionRepository = auctionRepository;
                this.auctionRegistrationMapper = auctionRegistrationMapper;
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getAllRegistrations() {
                List<AuctionParticipationInfo> entities = participationInfoRepository.findAll();
                List<AuctionRegistrationResponse> responses = auctionRegistrationMapper.toResponseList(entities);
                return responses;
        }

        @Transactional(readOnly = true)
        public AuctionRegistrationResponse getRegistrationById(String id) {
                // 1. Tim entity tu Database
                AuctionParticipationInfo registration = participationInfoRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

                // 2. Su dung mapper de chuyen doi va tra ve
                return auctionRegistrationMapper.toResponse(registration);
        }

        @Transactional
        public AuctionRegistrationResponse registerForAuction(AuctionRegistrationCreateRequest request, String userId) {
                // 1. Kiem tra User
                Customer user = customerRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // 2. Kiem tra Phien dau gia
                Auction auction = auctionRepository.findById(request.auctionId())
                                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

                // 3. Kiem tra nguoi ban khong duoc tu dang ky
                if (auction.getCreator() != null && auction.getCreator().getId().equals(userId)) {
                        throw new IllegalArgumentException("Người tạo phiên đấu giá không thể đăng ký tham gia");
                }

                // 4. Kiem tra dang ky trung lap (Logic cu giu nguyen)
                boolean alreadyRegistered = Optional.ofNullable(auction.getParticipants())
                                .orElse(Collections.emptyList())
                                .stream()
                                .anyMatch(reg -> reg.getParticipant().getId().equals(userId));

                if (alreadyRegistered) {
                        throw new IllegalArgumentException("User is already registered for this auction");
                }

                // 4. Tao va luu dang ky
                AuctionParticipationInfo registration = new AuctionParticipationInfo(user, auction);
                AuctionParticipationInfo savedRegistration = participationInfoRepository.save(registration);

                return auctionRegistrationMapper.toResponse(savedRegistration);
        }

        @Transactional
        public void deleteRegistration(String id) {
                AuctionParticipationInfo registration = participationInfoRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
                participationInfoRepository.delete(registration);
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getRegistrationsByUserId(String userId) {
                // 1. Kiem tra User ton tai
                if (!customerRepository.existsById(userId)) {
                        throw new IllegalArgumentException("User not found");
                }

                // 2. Lay du lieu da loc tu DB (Thay vi lay tat ca roi filter)
                List<AuctionParticipationInfo> entities = participationInfoRepository.findByParticipantId(userId);

                // 3. Su dung mapper de chuyen doi toan bo danh sach
                return auctionRegistrationMapper.toResponseList(entities);
        }

        @Transactional(readOnly = true)
        public AuctionRegistrationResponse getRegistrationByUserAndAuction(String userId, String auctionId) {
                AuctionParticipationInfo registration = participationInfoRepository
                                .findByParticipantIdAndAuctionId(userId, auctionId)
                                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
                return auctionRegistrationMapper.toResponse(registration);
        }

        @Transactional(readOnly = true)
        public List<AuctionRegistrationResponse> getRegistrationsByAuctionId(String auctionId) {
                // 1. Kiem tra phien dau gia co ton tai khong
                if (!auctionRepository.existsById(auctionId)) {
                        throw new IllegalArgumentException("Auction not found");
                }

                // 2. Lay du lieu tu DB thong qua phuong thuoc moi o Repository
                List<AuctionParticipationInfo> entities = participationInfoRepository
                                .findByAuctionId(auctionId);

                // 3. Su dung Mapper de chuyen doi sang danh sach Response
                return auctionRegistrationMapper.toResponseList(entities);
        }
}
