import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest, { params }: {
    params: Promise<{ provider: string }>
}) {
    const { provider } = await params;
    const searchParams = request.nextUrl.searchParams;
    const successUrl = new URL(`/auth-success`, request.url);
    const failedUrl = new URL(`/auth-failed`, request.url);
    if (provider === 'google') {
        const code = searchParams.get('code')
        try {
            const req: RequestInit = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    'code': code,
                })
            }
            const res = await ServerAPICall<JWTResponse>("/auth/google", req);
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
                return NextResponse.redirect(successUrl);
            }
            throw new Error(res.errorMessage || "Failed to sign in");
        }
        catch (e) {
            console.error(e);
            return NextResponse.redirect(failedUrl);
        }
    }
}