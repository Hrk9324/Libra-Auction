'use server';
import { getJWTPublicKey } from "./get_cert";
import * as jose from "jose";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { JWTResponse } from "@/types/jwt_response";

interface RefreshRouteResponse {
    message: string;
    tokenInfo: JWTResponse;
}

export async function refreshToken(): Promise<JWTResponse | null> {
    const jwtTokenInfo = await getJWTTokenInfo();
    const alg = 'RS256';
    const spki = await getJWTPublicKey();
    if (spki && jwtTokenInfo.refresh) {
        const publicKey = await jose.importSPKI(spki, alg);
        try {
            await jose.jwtVerify(jwtTokenInfo.refresh, publicKey);
            const response = await fetch('http://localhost:3000/api/refresh', {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    'refreshToken': jwtTokenInfo.refresh
                })
            });
            if (!response.ok) {
                return null;
            }
            const data = await response.json() as RefreshRouteResponse;
            return data.tokenInfo;
        } catch (error) {
            if (error instanceof JWTExpired) {
                console.log("Refresh token expired");
            }
            else if (error instanceof JWSSignatureVerificationFailed) {
                console.log("Invalid refresh token");
            }
            else {
                console.error("Can't refresh token: " + error)
            }
            return null;
        }
    }
    return null;
}