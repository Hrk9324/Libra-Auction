'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { UserInfo } from "@/types/user_info";

export async function fetchUserInfo(user_id: string): Promise<UserInfo> {
    const request: RequestInit = {
        method: "GET",
        headers: {}
    }
    const res = await ServerAPIAuthedCall<UserInfo>("/api/users/" + user_id, request);
    if (res.isSuccess && res.data) return res.data;
    throw createAppErrorFromResponse(res, "Failed to fetch user info");
}