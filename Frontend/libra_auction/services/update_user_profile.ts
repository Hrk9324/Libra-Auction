'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { UserInfo } from "@/types/user_info";

export interface UpdateProfileRequest {
  fullName?: string;
  phoneNumber?: string;
  identityNumber?: string;
  avatarUrl?: string;
}

export async function updateUserProfile(
  userId: string,
  data: UpdateProfileRequest
): Promise<UserInfo> {

  const request: RequestInit = {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  };

  const res = await ServerAPIAuthedCall<UserInfo>("/api/users/" + userId, request);
  if (res.isSuccess && res.data) return res.data;
  throw createAppErrorFromResponse(res, "Failed to update user profile");
}
