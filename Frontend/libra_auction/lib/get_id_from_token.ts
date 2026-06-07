'use server';
import { clearAuthCookies } from "./clear_auth_cookies";
import { getJWTPublicKey } from "./get_cert";
import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { refreshToken } from "./refresh_token";
import { getJWTTokenInfo } from "./get_jwt_token_info";

export async function getIdFromToken(): Promise<string | null> {
    const jwtTokenInfo = await getJWTTokenInfo();
    const alg = 'RS256';
    const spki = await getJWTPublicKey();
    if (spki && jwtTokenInfo.token) {
        const publicKey = await jose.importSPKI(spki, alg);
        try {
            const { payload } = await jose.jwtVerify(jwtTokenInfo.token, publicKey);
            return payload.sub as string;
        }
        catch (error) {
            if (error instanceof JWTExpired) {
                console.log("Token expired");
                const refreshed = await refreshToken();
                if (!refreshed) {
                    return null;
                }
                const { payload } = await jose.jwtVerify(refreshed.token, publicKey);
                return payload.sub as string;
            }

            if (error instanceof JWSSignatureVerificationFailed) {
                console.log("Invalid token");
            }
            await clearAuthCookies();
            return null;
        }
    }
    return null;
}
