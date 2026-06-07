'use server';
import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPICall } from "@/lib/server_API_call";
import { Auction } from "@/types/auction/auction";

export async function fetchAuctionDetails(auctionId: string): Promise<Auction> {
  const request: RequestInit = {
    method: "GET",
    headers: {},
  };

  const res = await ServerAPICall<Auction>(
    "/api/public/auctions/" + auctionId,
    request
  );

  if (res.isSuccess && res.data) return res.data;
  throw createAppErrorFromResponse(res, "Failed to fetch auction details");
}
