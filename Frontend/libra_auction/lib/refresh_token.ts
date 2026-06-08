'use server';

import { cookies } from "next/headers";
import { clearAuthCookies } from "./clear_auth_cookies";
import { getJWTPublicKey } from "./get_cert";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";

export async function refreshToken(): Promise<JWTResponse | null> {
    const jwtTokenInfo = await getJWTTokenInfo();
    const alg = 'RS256';
    const spki = await getJWTPublicKey();
    
    if (spki && jwtTokenInfo.refresh) {
        const publicKey = await jose.importSPKI(spki, alg);
        try {
            await jose.jwtVerify(jwtTokenInfo.refresh, publicKey);
            
            const res = await ServerAPICall<JWTResponse>("/auth/refresh", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    'refreshToken': jwtTokenInfo.refresh
                })
            });

            if (res.isSuccess && res.data) {
                const cookieStore = await cookies();
                
                cookieStore.set({
                    name: 'jwtToken',
                    value: res.data.token,
                    httpOnly: true,
                    secure: true,
                    maxAge: Math.floor(res.data.accessTokenExpiration)
                });
                
                cookieStore.set({
                    name: 'refreshToken',
                    value: res.data.refreshToken,
                    httpOnly: true,
                    secure: true,
                    maxAge: Math.floor(res.data.refreshTokenExpiration)
                });
                
                return res.data; 
            }
            
            await clearAuthCookies();
            return null;

        } catch (error) {
            if (error instanceof JWTExpired) {
                console.log("Refresh token expired");
            }
            else if (error instanceof JWSSignatureVerificationFailed) {
                console.log("Invalid refresh token");
            }
            else {
                console.error("Can't refresh token: " + error);
            }
            
            await clearAuthCookies();
            return null;
        }
    }

    await clearAuthCookies();
    return null;
}