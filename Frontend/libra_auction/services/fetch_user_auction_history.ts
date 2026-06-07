'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";

export interface AuctionRegistrationResponse {
  id: string;
  userId: string;
  email: string;
  auctionId: string;
  registrationTime: string;
}

export async function fetchUserAuctionHistory(userId: string): Promise<AuctionRegistrationResponse[]> {
  const request: RequestInit = {
    method: "GET",
    cache: "no-store",
  };

  const res = await ServerAPIAuthedCall<AuctionRegistrationResponse[]>(
    "/api/auction-registrations/user/" + userId,
    request
  );

  if (res.isSuccess && Array.isArray(res.data)) {
    return res.data;
  }

  throw createAppErrorFromResponse(res, "Failed to fetch auction history");
}
