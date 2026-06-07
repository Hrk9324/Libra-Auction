'use server';

import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { AdminAuction } from "@/types/admin/admin_auction";

export async function rejectAuction(auctionId: string, reason?: string): Promise<AdminAuction> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(reason ? { reason } : {}),
    };

    const res = await ServerAPIAuthedCall<AdminAuction>(`/api/admin/auctions/${auctionId}/reject`, request);
    if (res.isSuccess && res.data) {
        return res.data;
    }

    throw createAppErrorFromResponse(res, "Failed to reject auction");
}