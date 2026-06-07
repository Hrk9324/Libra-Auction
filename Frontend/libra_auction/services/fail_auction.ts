'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";

export async function failAuction(auctionId: string, reason: string): Promise<boolean> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ reason }),
    };
    const res = await ServerAPIAuthedCall<null>(`/api/admin/auctions/${auctionId}/fail`, request);
    if (res.isSuccess) {
        return true;
    }
    throw createAppErrorFromResponse(res, "Failed to mark auction as failed");
}
