'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { Auction } from "@/types/auction/auction";
import { NewAuction } from "@/types/auction/new-auction";

export async function createAuction(auction: NewAuction): Promise<Auction> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(
            auction
        )
    }
    const res = await ServerAPIAuthedCall<Auction>("/api/auctions", request);
    if (res.isSuccess && res.data) {
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to create auctions");
}