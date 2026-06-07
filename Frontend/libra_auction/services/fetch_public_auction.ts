'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { Auction } from "@/types/auction/auction";

export async function fetchPublicAuction(auction_id: string): Promise<Auction> {
    const request: RequestInit = {
        method: "GET",
    }
    const res = await ServerAPICall<Auction>("/api/public/auctions/" + auction_id, request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to fetch auction");
}