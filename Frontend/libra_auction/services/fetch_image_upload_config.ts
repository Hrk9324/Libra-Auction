'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { ImageUploadConfig } from "@/types/image_upload_config";

export async function fetchImageUploadConfig(folder: string, fileName: string): Promise<ImageUploadConfig> {
    const request: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            folder: folder,
            fileName: fileName
        })
    }
    const res = await ServerAPICall<ImageUploadConfig>("/api/public/images/generate-upload-url", request);
    if (res.isSuccess && res.data) return res.data;
    throw createAppErrorFromResponse(res, "Failed to fetch image upload config");
}