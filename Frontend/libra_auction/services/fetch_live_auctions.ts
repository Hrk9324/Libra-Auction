'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { mapAuctionToLiveAuction } from "@/mappers/map_auction_to_live_auction";
import { Auction } from "@/types/auction/auction";
import { LiveAuction } from "@/types/auction/live_auction";
import { PageResponse } from "@/types/page_response";

export async function fetchLiveAuctions(): Promise<LiveAuction[]> {
    const request: RequestInit = {
        method: "GET",
    }
    const res = await ServerAPICall<PageResponse<Auction>>("/api/public/auctions?status=IN_PROGRESS", request);
    if (res.isSuccess && res.data) {
        return res.data.content.map((i) => mapAuctionToLiveAuction(i));
    }
    else if(res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch live auctions");
}