'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { NewProduct } from "@/types/product/new-product";
import { Product } from "@/types/product/product";

export async function createProduct(product: NewProduct): Promise<Product> {

    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(
            product
        )
    }
    const res = await ServerAPIAuthedCall<Product>("/api/products", request);
    if (res.isSuccess && res.data) {
        console.log(res.data);
        return res.data;
    }
    throw createAppErrorFromResponse(res, "Failed to create products");
}