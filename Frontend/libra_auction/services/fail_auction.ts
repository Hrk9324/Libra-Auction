'use server';
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";

export async function failAuction(auctionId: string, reason: string): Promise<boolean> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        throw new Error("Admin credentials not found");
    }

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + jwtTokenInfo.token,
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ reason }),
    };
    const res = await ServerAPICall<null>(`/api/admin/auctions/${auctionId}/fail`, request);
    if (res.isSuccess) {
        return true;
    }
    throw new Error(res.errorMessage || "Failed to mark auction as failed");
}
