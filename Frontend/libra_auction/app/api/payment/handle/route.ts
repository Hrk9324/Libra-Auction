import { NextRequest, NextResponse } from "next/server";
import { fetchPublicAuction } from "@/services/fetch_public_auction";

export async function GET(req: NextRequest) {
    const searchParams = req.nextUrl.searchParams;
    const auctionId = searchParams.get("auctionId");
    const status = searchParams.get("status") || "failed";

    if (!auctionId) {
        return NextResponse.redirect(new URL("/", req.url));
    }

    try {
        const auction = await fetchPublicAuction(auctionId);
        return NextResponse.redirect(
            new URL(`/auctions/${auction.category_id}/${auctionId}/registration?status=${status}`, req.url)
        );
    } catch {
        // Fallback: redirect to home if auction fetch fails
        return NextResponse.redirect(
            new URL(`/?payment=${status}`, req.url)
        );
    }
}
