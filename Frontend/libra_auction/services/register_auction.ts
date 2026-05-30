'use server';
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";
import { AuctionRegistration } from "@/types/auction/auction_registration";

export async function registerForAuction(auctionId: string): Promise<AuctionRegistration> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        throw new Error("User's credentials not found");
    }

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + jwtTokenInfo.token,
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ auctionId }),
    };
    const res = await ServerAPICall<AuctionRegistration>("/api/auction-registrations", request);
    if (res.isSuccess && res.data) {
        return res.data;
    }
    throw new Error(res.errorMessage || "Registration failed");
}

export async function checkRegistration(userId: string, auctionId: string): Promise<AuctionRegistration | null> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        return null;
    }

    const request: RequestInit = {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + jwtTokenInfo.token,
        },
    };
    const res = await ServerAPICall<AuctionRegistration>(
        `/api/auction-registrations/user/${userId}/auction/${auctionId}`,
        request
    );
    if (res.isSuccess && res.data) {
        return res.data;
    }
    return null;
}
