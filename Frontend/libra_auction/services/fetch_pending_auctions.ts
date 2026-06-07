'use server';

import { createAppErrorFromResponse } from "@/lib/app_error";
import { ServerAPIAuthedCall } from "@/lib/server_API_authed_call";
import { AdminAuction } from "@/types/admin/admin_auction";
import { PageResponse } from "@/types/page_response";

export async function fetchPendingAuctions(page: number = 0, pageSize: number = 20): Promise<PageResponse<AdminAuction>> {

    const request: RequestInit = {
        method: "GET",
        headers: {}
    };

    const res = await ServerAPIAuthedCall<PageResponse<AdminAuction>>(
        `/api/admin/auctions/pending?page=${page}&pageSize=${pageSize}`,
        request,
    );

    if (res.isSuccess && res.data) {
        return res.data;
    }

    if (res.isSuccess) {
        return {
            content: [],
            totalPages: 0,
            totalElements: 0,
            currentPage: page,
            pageSize,
            isFirst: true,
            isLast: true,
        };
    }

    throw createAppErrorFromResponse(res, "Failed to fetch pending auctions");
}