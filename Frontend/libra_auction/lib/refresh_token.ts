'use server';
import { getJWTPublicKey } from "./get_cert";
import * as jose from "jose";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";

export async function refreshToken() {
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
            return response.ok;
        } catch (error) {
            if (error instanceof JWTExpired) {
                console.log("Token expired");
            }
            else if (error instanceof JWSSignatureVerificationFailed) {
                console.log("Invalid token");
            }
            else {
                console.error("Can't refresh token: " + error)
            }
            return false;
        }
    }
    return false;
}