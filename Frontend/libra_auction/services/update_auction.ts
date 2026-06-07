'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { NewAuction } from "@/types/auction/new-auction";
import { Auction } from "@/types/auction/auction";

export async function updateAuction(auction_id: string, auction: NewAuction): Promise<Auction> {

    const request: RequestInit = {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(
            auction
        )
    }
    const res = await ServerAPIAuthedCall<Auction>("/api/auctions/" + auction_id, request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to update auctions");
}