'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { Auction } from "@/types/auction/auction";
import { PageResponse } from "@/types/page_response";

export async function fetchPublicAuctions(
    categoryId?: string,
    name?: string,
    status?: string,
    priceFrom?: string,
    priceTo?: string,
): Promise<Auction[]> {
    const request: RequestInit = {
        method: "GET",
    }
    const query = new URLSearchParams();

    if (name) {
        query.set("name", name);
    }

    if (status) {
        query.set("status", status);
    }

    if (priceFrom) {
        query.set("priceFrom", priceFrom);
    }

    if (priceTo) {
        query.set("priceTo", priceTo);
    }

    const queryString = query.toString();
    const endpoint = categoryId
        ? `/api/public/categories/${categoryId}/auctions${queryString ? `?${queryString}` : ""}`
        : `/api/public/auctions${queryString ? `?${queryString}` : ""}`;
    const res = await ServerAPICall<PageResponse<Auction>>(endpoint, request);
    if (res.isSuccess && res.data) {
        return res.data.content;
    }
    else if(res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch live auctions");
}