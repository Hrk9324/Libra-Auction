'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { AuctionRegistration } from "@/types/auction/auction_registration";

export async function registerForAuction(auctionId: string): Promise<AuctionRegistration> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ auctionId }),
    };
    const res = await ServerAPIAuthedCall<AuctionRegistration>("/api/auction-registrations", request);
    if (res.isSuccess && res.data) {
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Registration failed");
}

export async function checkRegistration(userId: string, auctionId: string): Promise<AuctionRegistration | null> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        return null;
    }

    const request: RequestInit = {
        method: "GET",
        headers: {},
    };
    const res = await ServerAPIAuthedCall<AuctionRegistration>(
        `/api/auction-registrations/user/${userId}/auction/${auctionId}`,
        request
    );
    if (res.isSuccess && res.data) {
        return res.data;
    }
    return null;
}
