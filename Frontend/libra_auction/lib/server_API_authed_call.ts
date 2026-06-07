'use server';

import { AppError } from "@/lib/app_error";
import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";
import type { ServerAPIResponse } from "@/types/serser_API_response";

export async function ServerAPIAuthedCall<T>(path: string, request: RequestInit): Promise<ServerAPIResponse<T>> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        throw new AppError(401, "Bạn cần đăng nhập để tiếp tục.");
    }

    const headers = new Headers(request.headers);
    headers.set("Authorization", "Bearer " + jwtTokenInfo.token);

    return ServerAPICall<T>(path, {
        ...request,
        headers,
    });
}
