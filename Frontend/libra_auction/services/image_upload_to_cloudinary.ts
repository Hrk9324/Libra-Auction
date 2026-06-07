'use client';
import { AppError, getErrorMessage } from "@/lib/app_error";
import { ImageUploadConfig } from "@/types/image_upload_config";

export async function uploadImageToCloudinary(file: File, config: ImageUploadConfig): Promise<string | undefined> {
    const formData = new FormData();

    // 1. Add the actual image file
    formData.append("file", file);

    // 2. Add the basic security information
    formData.append("api_key", config.apiKey); 
    formData.append("signature", config.signature);
    formData.append("timestamp", config.timestamp.toString());

    // 3. Forward all extra params from the backend
    Object.keys(config.additionalParams).forEach((key) => {
        formData.append(key, String(config.additionalParams[key]));
    });

    try {
        const response = await fetch(config.uploadUrl, {
            method: "POST",
            body: formData,
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new AppError(response.status, errorData.error?.message || "Upload failed");
        }

        const result = await response.json();
        return result.secure_url;
    } catch (error) {
        if (error instanceof AppError) throw error;
        throw new AppError(503, getErrorMessage(error, "Upload failed"));
    }
}