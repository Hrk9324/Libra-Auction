'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { mapAuctionToUpcoming } from "@/mappers/map_auction_to_upcoming_auction";
import { Auction } from "@/types/auction/auction";
import { PageResponse } from "@/types/page_response";
import { UpcomingAuction } from "@/types/auction/upcoming_auction";

export async function fetchUpcomingAuctions(): Promise<UpcomingAuction[]> {
    const request: RequestInit = {
        method: "GET",
    }
    const res = await ServerAPICall<PageResponse<Auction>>("/api/public/auctions?status=NOT_STARTED", request);
    if (res.isSuccess && res.data) {
        return res.data.content.map((i) => mapAuctionToUpcoming(i));
    }
    else if(res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch upcoming auctions");
}