import { Auction } from "@/types/auction/auction";
import { LiveAuction } from "@/types/auction/live_auction";

export const mapAuctionToLiveAuction = (auction: Auction): LiveAuction => {
  // 1. Tính toán thời gian còn lại (ms)
  // duration tính bằng giây (theo backend)
  const endTime = new Date(auction.start_time).getTime() + (auction.duration * 1000);
  const now = Date.now();
  const timeLeft = Math.max(0, endTime - now);

  return {
    id: auction.auction_id,
    
    thumbnail: auction.images && auction.images.length > 0 
      ? auction.images[0] 
      : "/placeholder-image.jpg", 
    
    title: auction.auction_name,
    current_bid: auction.current_price,
    bids: auction.total_bids,
    time_left: timeLeft,
    category_id: auction.category_id,
  };
};