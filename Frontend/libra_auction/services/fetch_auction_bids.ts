'use server';
import { ServerAPICall } from "@/lib/server_API_call";

export interface AuctionBid {
    auctionId: string;
    bidAmount: number;
    bidderId: string;
    bidderName: string;
    bidTime: string;
    status: string;
}

export async function fetchAuctionBids(auctionId: string): Promise<AuctionBid[]> {
    const request: RequestInit = {
        method: "GET",
    };
    const res = await ServerAPICall<AuctionBid[]>(`/api/public/auctions/${auctionId}/bids`, request);
    if (res.isSuccess && res.data) {
        return res.data;
    }
    return [];
}
