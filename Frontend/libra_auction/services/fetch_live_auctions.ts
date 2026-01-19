import type { LiveAuctionCardType } from "@/types/live_auction_card_type";
export function FetchLiveAuctions(): LiveAuctionCardType[] {
  // TODO: Fetch data from api
  return [
    {
      id: "001",
      image_src: "/live-auction-1.jpg",
      title: "2015 TOYOTA 4RUNNER SR5/SR5 PREMIUM",
      current_bid: 1182082000,
      bids: 18,
      time_left: 1000000,
    },
    {
      id: "002",
      image_src: "/live-auction-2.jpg",
      title: "H14-7. Selflessness, from The Secrets, 2024",
      current_bid: 141050000,
      bids: 27,
      time_left: 8400000,
    },
    {
      id: "003",
      image_src: "/live-auction-3.jpg",
      title: "Star Rail Hotaru Spring Gift Ver. 1/8 Scale Complete Figure",
      current_bid: 1210000,
      bids: 66,
      time_left: 2000000,
    },
  ];
}
