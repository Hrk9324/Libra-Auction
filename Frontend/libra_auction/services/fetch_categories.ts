'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { Category } from "@/types/category";

export async function fetchCategories(): Promise<Category[]> {
    const request: RequestInit = {
        method: "GET",
    }
    const res = await ServerAPICall<Category[]>("/api/public/categories", request);
    if (res.isSuccess && res.data) return res.data;
    else if(res.isSuccess) return [];
    throw createAppErrorFromResponse(res, "Failed to fetch categories");
}