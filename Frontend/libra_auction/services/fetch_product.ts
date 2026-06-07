'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { Product } from "@/types/product/product";

export async function fetchProduct(product_id: string): Promise<Product> {
    const request: RequestInit = {
        method: "GET",
        headers: {}
    }
    const res = await ServerAPIAuthedCall<Product>("/api/products/" + product_id, request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to fetch product");
}