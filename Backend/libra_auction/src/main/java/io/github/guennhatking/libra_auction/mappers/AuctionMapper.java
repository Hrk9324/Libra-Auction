package io.github.guennhatking.libra_auction.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.product.Product;
import io.github.guennhatking.libra_auction.viewmodels.response.AttributeResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;

@Mapper(componentModel = "spring", uses = { ProductImageMapper.class })
public interface AuctionMapper {

    @Mapping(source = "product.category.id", target = "category_id", defaultValue = "uncategorized")
    @Mapping(source = "product.category.name", target = "category_name")
    @Mapping(source = "id", target = "auction_id")

    @Mapping(source = "auctionStatus", target = "auction_status", defaultValue = "NOT_STARTED")
    @Mapping(source = "approvalStatus", target = "approval_status")
    @Mapping(source = "startTime", target = "start_time")
    @Mapping(source = "duration", target = "duration")
    @Mapping(source = "startingPrice", target = "starting_price")
    @Mapping(target = "current_price", expression = "java(session.getCurrentPrice() > 0 ? session.getCurrentPrice() : session.getStartingPrice())")
    @Mapping(source = "depositAmount", target = "deposit_amount")
    @Mapping(source = "minimumBidIncrement", target = "min_bid_increment")

    @Mapping(source = "product.id", target = "product_id")
    @Mapping(source = "product.name", target = "product_name")
    @Mapping(source = "product.quantity", target = "quantity")
    @Mapping(source = "product.description", target = "description")

    @Mapping(source = "product.images", target = "images")
    @Mapping(source = ".", target = "auction_name", qualifiedByName = "resolveAuctionTitle")
    @Mapping(source = ".", target = "attributes", qualifiedByName = "resolveAttributes")
    @Mapping(source = ".", target = "total_bids", qualifiedByName = "resolveTotalBids")
    @Mapping(source = ".", target = "total_participants", qualifiedByName = "resolveTotalParticipants")
    @Mapping(source = "failureReason", target = "failure_reason")
    @Mapping(source = "completedAt", target = "completed_at")
    @Mapping(source = "creator.id", target = "creator_id")
    AuctionResponse toAuctionResponse(Auction session);

    List<AuctionResponse> toAuctionResponseList(List<Auction> sessions);

    @Named("resolveAttributes")
    default List<AttributeResponse> resolveAttributes(Auction session) {
        if (session == null || session.getProduct() == null)
            return Collections.emptyList();

        List<AttributeResponse> results = new ArrayList<>();
        Product product = session.getProduct();

        // Get from ProductAttributes (isSystem = false)
        if (product.getAttributes() != null) {
            product.getAttributes().forEach(
                    attr -> results.add(new AttributeResponse(attr.getAttributeName(), attr.getAttributeValue(), false)));
        }

        // Get from AttributeCombinations -> StandardizedAttribute (isSystem = true)
        if (product.getAttributeCombinations() != null) {
            product.getAttributeCombinations().forEach(kh -> {
                if (kh.getStandardizedAttribute() != null) {
                    results.add(new AttributeResponse(
                            kh.getStandardizedAttribute().getAttributeName(),
                            kh.getStandardizedAttribute().getAttributeValue(),
                            true));
                }
            });
        }

        return results;
    }

    @Named("resolveTotalBids")
    default Long resolveTotalBids(Auction session) {
        if (session == null || session.getBidHistory() == null)
            return 0L;
        return (long) session.getBidHistory().size();
    }

    @Named("resolveTotalParticipants")
    default Long resolveTotalParticipants(Auction session) {
        if (session == null || session.getParticipants() == null)
            return 0L;
        return (long) session.getParticipants().size();
    }

    @Named("resolveAuctionTitle")
    default String resolveAuctionTitle(Auction session) {
        if (session == null || session.getProduct() == null) {
            return "Untitled item";
        }

        String productName = session.getProduct().getName();
        return productName != null && !productName.trim().isEmpty()
                ? productName
                : "Untitled item";
    }
}
