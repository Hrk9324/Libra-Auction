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
            const res = await fetch(process.env.BACKEND_SERVER_URL! + '/auth/google', {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    'code': code,
                })
            });
            const data = await res.json();
            if (!res.ok) {
                return NextResponse.redirect(failedUrl);
            }
            const jwtToken = data.token;
            const refreshToken = data.refreshToken;
            const cookieStore = await cookies();
            cookieStore.set({
                name: 'jwtToken',
                value: jwtToken,
                httpOnly: true,
                secure: true,
                maxAge: 60 * 60 * 24 // 1 day
            });
            cookieStore.set({
                name: 'refreshToken',
                value: refreshToken,
                httpOnly: true,
                secure: true,
                maxAge: 60 * 60 * 24 * 7 // 7 days
            })
            return NextResponse.redirect(successUrl);
        }
        catch (error) {
            return NextResponse.redirect(failedUrl);
        }
    }
    return NextResponse.redirect(failedUrl);
}