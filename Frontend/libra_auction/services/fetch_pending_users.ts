'use server';

import { getJWTTokenInfo } from "@/lib/get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";
import { PageResponse } from "@/types/page_response";
import { PendingUser } from "@/types/admin/pending_user";

export type AdminUserFilters = {
    name?: string;
    email?: string;
    phone?: string;
    identityNumber?: string;
    emailStatus?: string;
    accountStatus?: string;
};

function buildUsersQuery(page: number, pageSize: number, filters: AdminUserFilters = {}) {
    const params = new URLSearchParams({
        page: String(page),
        pageSize: String(pageSize),
    });

    Object.entries(filters).forEach(([key, value]) => {
        if (value && value !== "ALL") {
            params.set(key, value);
        }
    });

    return params.toString();
}

async function getAuthorizedRequest(method: "GET" | "POST"): Promise<RequestInit> {
    const jwtTokenInfo = await getJWTTokenInfo();
    if (!jwtTokenInfo.token) {
        throw new Error("User's credentials not found");
    }

    return {
        method,
        headers: {
            "Authorization": "Bearer " + jwtTokenInfo.token
        }
    };
}

async function fetchUsersByPath(path: string, request: RequestInit, page: number, pageSize: number): Promise<PageResponse<PendingUser>> {
    const res = await ServerAPICall<PageResponse<PendingUser>>(path, request);

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

    throw new Error(res.errorMessage || "Failed to fetch users");
}

export async function fetchAdminUsers(
    page: number = 0,
    pageSize: number = 100,
    filters: AdminUserFilters = {},
): Promise<PageResponse<PendingUser>> {
    const request = await getAuthorizedRequest("GET");

    return fetchUsersByPath(
        `/api/admin/users?${buildUsersQuery(page, pageSize, filters)}`,
        request,
        page,
        pageSize,
    );
}

export async function fetchPendingUsers(page: number = 0, pageSize: number = 20): Promise<PageResponse<PendingUser>> {
    const request = await getAuthorizedRequest("GET");
    return fetchUsersByPath(
        `/api/admin/users/pending?page=${page}&pageSize=${pageSize}`,
        request,
        page,
        pageSize,
    );
}

export async function updateAdminUserAction(userId: string, action: "approve" | "reject" | "lock" | "unlock"): Promise<PendingUser> {
    const request = await getAuthorizedRequest("POST");
    const res = await ServerAPICall<PendingUser>(`/api/admin/users/${userId}/${action}`, request);

    if (res.isSuccess && res.data) {
        return res.data;
    }

    throw new Error(res.errorMessage || `Failed to ${action} user (status ${res.status})`);
}
