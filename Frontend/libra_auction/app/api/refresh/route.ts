import { clearAuthCookies } from "@/lib/clear_auth_cookies";
import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
    try {
        const body = await request.json();
        const { refreshToken } = body;

        const req: RequestInit = {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                'refreshToken': refreshToken
            })
        };

        const res = await ServerAPICall<JWTResponse>("/auth/refresh", req);
        if (res.isSuccess && res.data) {
            const jwtToken = res.data.token;
            const cookieStore = await cookies();
            cookieStore.set({
                name: 'jwtToken',
                value: jwtToken,
                httpOnly: true,
                secure: true,
                maxAge: Math.floor(res.data.accessTokenExpiration / 1000)
            });
            cookieStore.set({
                name: 'refreshToken',
                value: res.data.refreshToken,
                httpOnly: true,
                secure: true,
                maxAge: Math.floor(res.data.refreshTokenExpiration / 1000)
            });
            return NextResponse.json({ message: "Refresh successful", tokenInfo: res.data }, { status: 200 });
        }

        await clearAuthCookies();
        return NextResponse.json({ message: res.errorMessage || "Failed to refresh token" }, { status: 401 });
    }
    catch (error) {
        console.error(error);
        await clearAuthCookies();
        return NextResponse.json({ message: "Failed to refresh token" }, { status: 401 });
    }
}
