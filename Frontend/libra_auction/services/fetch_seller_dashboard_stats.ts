'use server';

import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import type { SellerDashboardStats } from "@/types/dashboard_stats";

export async function fetchSellerDashboardStats(): Promise<SellerDashboardStats> {
  const request: RequestInit = {
    method: "GET",
    headers: {},
  };

  const res = await ServerAPIAuthedCall<SellerDashboardStats>("/api/dashboard/seller", request);

  if (res.isSuccess && res.data) {
    return res.data;
  }

  throw createAppErrorFromResponse(res, "Failed to fetch seller dashboard stats");
}
