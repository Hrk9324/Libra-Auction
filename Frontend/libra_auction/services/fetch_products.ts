'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { PageResponse } from "@/types/page_response";
import { Product } from "@/types/product/product";

export async function fetchProducts(): Promise<Product[]> {

    const request: RequestInit = {
        method: "GET",
        headers: {}
    }
    const res = await ServerAPIAuthedCall<PageResponse<Product>>("/api/products", request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data.content;
    }
    else if (res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch products");
}