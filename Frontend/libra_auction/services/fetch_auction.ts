'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { Auction } from "@/types/auction/auction";

export async function fetchAuction(auction_id: string): Promise<Auction> {

    const request: RequestInit = {
        method: "GET",
        headers: {}
    }
    const res = await ServerAPIAuthedCall<Auction>("/api/auctions/" + auction_id, request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to fetch auction");
}