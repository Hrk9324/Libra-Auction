'use server';

import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import type { AdminDashboardStats } from "@/types/dashboard_stats";

export async function fetchAdminDashboardStats(): Promise<AdminDashboardStats> {
  const request: RequestInit = {
    method: "GET",
    headers: {},
  };

  const res = await ServerAPIAuthedCall<AdminDashboardStats>("/api/dashboard/admin", request);

  if (res.isSuccess && res.data) {
    return res.data;
  }

  throw createAppErrorFromResponse(res, "Failed to fetch admin dashboard stats");
}
