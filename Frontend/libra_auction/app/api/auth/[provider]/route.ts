import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest, { params }: {
    params: Promise<{ provider: string }>
}) {
    const { provider } = await params;
    if (provider === 'google') {
        const clientId = process.env.GOOGLE_CLIENT_ID;
        console.log('Google Client ID:', clientId);
        const redirectURI = process.env.GOOGLE_REDIRECT_URI;
        console.log('Google Redirect URI:', redirectURI);
        const scope = process.env.GOOGLE_OAUTH_SCOPE;
        console.log('Google OAuth Scope:', scope);
        const options = {
            client_id: clientId!,
            redirect_uri: redirectURI!,
            response_type: 'code',
            scope: scope!,
            prompt: 'select_account',
        }
        const searchParams = new URLSearchParams(options).toString();
        const googleAuthUrl = 'https://accounts.google.com/o/oauth2/v2/auth?' + searchParams;
        return NextResponse.redirect(googleAuthUrl);
    }
}