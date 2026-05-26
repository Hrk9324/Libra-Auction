'use server';
import { ServerAPICall } from "@/lib/server_API_call";
import { Auction } from "@/types/auction/auction";
import { PageResponse } from "@/types/page_response";

export async function fetchPublicAuctions(categoryId?: string, name?: string, status?: string): Promise<Auction[]> {
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

    const queryString = query.toString();
    const endpoint = categoryId
        ? `/api/public/categories/${categoryId}/auctions${queryString ? `?${queryString}` : ""}`
        : `/api/public/auctions${queryString ? `?${queryString}` : ""}`;
    const res = await ServerAPICall<PageResponse<Auction>>(endpoint, request);
    if (res.isSuccess && res.data) {
        return res.data.content;
    }
    else if(res.isSuccess) return [];
    throw new Error(res.errorMessage || "Failed to fetch live auctions");
}