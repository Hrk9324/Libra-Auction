'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { Auction } from "@/types/auction/auction";
import { PageResponse } from "@/types/page_response";

export async function fetchAuctions(): Promise<Auction[]> {

    const request: RequestInit = {
        method: "GET",
        headers: {}
    }
    const res = await ServerAPIAuthedCall<PageResponse<Auction>>("/api/auctions", request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data.content;
    }
    else if (res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch auctions");
}