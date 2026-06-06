import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
    try {
        const body = await request.json();
        const { refreshToken } = body;
        console.log("RefreshToken: " + refreshToken);

        const req: RequestInit = {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                'refreshToken': refreshToken
            })
        }
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
            console.log("Success");
            return NextResponse.json({ message: "Refresh successful" }, { status: 200 })
        }
        throw new Error(res.errorMessage || "Failed to refresh token");
    }
    catch (error) {
        console.error(error);
        return NextResponse.json({ message: "Internal server error" }, { status: 500 })
    }
}