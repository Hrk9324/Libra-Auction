'use server';
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";

interface VNPayPaymentResponse {
    paymentUrl: string;
}

export async function createDeposit(auctionId: string): Promise<string> {
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
    const res = await ServerAPICall<VNPayPaymentResponse>("/api/payments/vnpay/create-deposit", request);
    if (res.isSuccess && res.data) {
        return res.data.paymentUrl;
    }
    throw new Error(res.errorMessage || "Failed to create deposit payment");
}

export async function isDepositPaid(auctionId: string): Promise<boolean> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        return false;
    }

    const request: RequestInit = {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + jwtTokenInfo.token,
        },
    };
    const res = await ServerAPICall<boolean>(`/api/payments/vnpay/deposit/status/${auctionId}`, request);
    if (res.isSuccess && res.data !== undefined) {
        return res.data;
    }
    return false;
}
