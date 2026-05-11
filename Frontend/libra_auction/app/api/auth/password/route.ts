import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";
export async function POST(request: NextRequest) {
    try {
        const body = await request.json();
        const { username, password } = body;
        const req: RequestInit = {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                'username': username,
                'password': password
            })
        }
        const res = await ServerAPICall<JWTResponse>("/auth/signin", req);
        if (res.isSuccess && res.data) {
            const jwtToken = res.data.token;
            const refreshToken = res.data.refreshToken;
            const cookieStore = await cookies();
            cookieStore.set({
                name: 'jwtToken',
                value: jwtToken,
                httpOnly: true,
                secure: true,
                maxAge: res.data.accessTokenExpiration
            });
            cookieStore.set({
                name: 'refreshToken',
                value: refreshToken,
                httpOnly: true,
                secure: true,
                maxAge: res.data.refreshTokenExpiration
            })
            return NextResponse.json({ message: "Sign in successful" }, { status: 200 })
        }
        throw new Error(res.errorMessage || "Failed to sign in");
    }
    catch (e) {
        console.error(e)
        return NextResponse.json({ message: "Intenal server error" }, { status: 500 });
    }
}