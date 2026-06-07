'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";

interface VNPayPaymentResponse {
    paymentUrl: string;
}

export async function createDeposit(auctionId: string): Promise<string> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ auctionId }),
    };
    const res = await ServerAPIAuthedCall<VNPayPaymentResponse>("/api/payments/vnpay/create-deposit", request);
    if (res.isSuccess && res.data) {
        return res.data.paymentUrl;
    }
    throw createAppErrorFromResponse(res, "Failed to create deposit payment");
}

export async function isDepositPaid(auctionId: string): Promise<boolean> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        return false;
    }

    const request: RequestInit = {
        method: "GET",
        headers: {},
    };
    const res = await ServerAPIAuthedCall<boolean>(`/api/payments/vnpay/deposit/status/${auctionId}`, request);
    if (res.isSuccess && res.data !== undefined) {
        return res.data;
    }
    return false;
}
