import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        return NextResponse.redirect(new URL("/sign-in", req.url));
    }

    try {
        const searchParams = req.nextUrl.searchParams;
        const paramsObj = Object.fromEntries(searchParams.entries());

        const requestConfig: RequestInit = {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + jwtTokenInfo.token,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(paramsObj),
        };

        // Determine endpoint based on order info
        const orderInfo = searchParams.get("vnp_OrderInfo") || "";
        let endpoint = "/api/payments/vnpay/deposit/successed";

        // Extract auction ID from order info for redirect
        let auctionId = "";
        // Order format: "Thanh toan tien coc cho dau gia: {auctionId}"
        const depositMatch = orderInfo.match(/dau gia:\s*(.+)/);
        if (depositMatch) {
            auctionId = depositMatch[1].trim();
        }

        const res = await ServerAPICall<boolean | null>(endpoint, requestConfig);

        const status = (res.isSuccess && res.data) ? "success" : "failed";

        if (auctionId) {
            return NextResponse.redirect(
                new URL(`/api/payment/handle?auctionId=${auctionId}&status=${status}`, req.url)
            );
        }

        // Fallback: redirect to home with status
        return NextResponse.redirect(
            new URL(`/?payment=${status}`, req.url)
        );

    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        console.error("VNPay verification error:", errorMessage);

        return NextResponse.json({
            success: false,
            message: errorMessage
        }, { status: 500 });
    }
}
