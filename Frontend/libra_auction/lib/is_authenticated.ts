'use server';
import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { clearAuthCookies } from "./clear_auth_cookies";
import { getJWTPublicKey } from "./get_cert";
import { refreshToken } from "./refresh_token";
import { getJWTTokenInfo } from "./get_jwt_token_info";

export async function isAuthenticated() {
    const jwtTokenInfo = await getJWTTokenInfo();
    const alg = 'RS256';
    const spki = await getJWTPublicKey();
    if (spki && jwtTokenInfo.token) {
        const publicKey = await jose.importSPKI(spki, alg);
        try {
            await jose.jwtVerify(jwtTokenInfo.token, publicKey);
        }
        catch (error) {
            if (error instanceof JWTExpired) {
                console.log("Token expired");
                return await refreshToken() !== null;
            }

            if (error instanceof JWSSignatureVerificationFailed) {
                console.log("Invalid token");
            }
            else {
                console.log("Can't verify token: " + error);
            }
            await clearAuthCookies();
            return false;
        }
        return true;
    }
    return false;
}
