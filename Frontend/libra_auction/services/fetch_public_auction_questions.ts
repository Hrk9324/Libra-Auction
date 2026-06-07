'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { AuctionQuestion } from "@/types/auction/auction_question";

export async function fetchPublicAuctionQuestions(auctionId: string): Promise<AuctionQuestion[]> {
    const request: RequestInit = {
        method: "GET",
    };

    const res = await ServerAPICall<AuctionQuestion[]>(`/api/public/auctions/${auctionId}/questions`, request);
    if (res.isSuccess && res.data) {
        return res.data;
    }

    if (res.isSuccess) {
        return [];
    }

    throw createAppErrorFromResponse(res, "Failed to fetch auction questions");
}