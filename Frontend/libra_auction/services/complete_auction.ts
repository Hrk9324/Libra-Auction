'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";

export async function completeAuction(auctionId: string): Promise<boolean> {

    const request: RequestInit = {
        method: "POST",
        headers: {},
    };
    const res = await ServerAPIAuthedCall<null>(`/api/admin/auctions/${auctionId}/complete`, request);
    if (res.isSuccess) {
        return true;
    }
    throw createAppErrorFromResponse(res, "Failed to complete auction");
}
