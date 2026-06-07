'use server';

import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import type { LiveNotification } from "@/types/notification/live_notification";

export async function fetchLiveNotifications(auctionId: string): Promise<LiveNotification[]> {
  const request: RequestInit = {
    method: "GET",
  };

  const res = await ServerAPICall<LiveNotification[]>(
    `/api/public/auctions/${auctionId}/live-notifications`,
    request
  );

  if (res.isSuccess && res.data) {
    return res.data;
  }

  if (res.isSuccess) return [];

  throw createAppErrorFromResponse(res, "Failed to fetch live notifications");
}