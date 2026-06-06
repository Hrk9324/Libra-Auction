 'use server';

import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { getJWTPublicKey } from "./get_cert";
import { refreshToken } from "./refresh_token";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { Role } from "@/types/user_info";

function toRole(rawRoleValue: unknown): Role | null {
	if (typeof rawRoleValue === "string" && rawRoleValue.length > 0) {
		return { name: rawRoleValue, description: "", permissions: [] };
	}
	// Backward compat: handle array from old tokens
	if (Array.isArray(rawRoleValue)) {
		const first = rawRoleValue.find((r): r is string => typeof r === "string" && r.length > 0);
		return first ? { name: first, description: "", permissions: [] } : null;
	}
	return null;
}

export async function getRole(): Promise<Role | null> {
	const jwtTokenInfo = await getJWTTokenInfo();
	const alg = "RS256";
	const spki = await getJWTPublicKey();

	if (spki && jwtTokenInfo.token) {
		const publicKey = await jose.importSPKI(spki, alg);
		try {
			const { payload } = await jose.jwtVerify(jwtTokenInfo.token, publicKey);
			const roleValue = payload.role ?? payload.roles;
			return toRole(roleValue);
		} catch (error) {
			if (error instanceof JWTExpired) {
				console.log("Token expired");
				const refreshed = await refreshToken();
				if (!refreshed) {
					return null;
				}
				const { payload } = await jose.jwtVerify(refreshed.token, publicKey);
				const roleValue = payload.role ?? payload.roles;
				return toRole(roleValue);
			}

			if (error instanceof JWSSignatureVerificationFailed) {
				console.log("Invalid token");
			}
		}
	}

	return null;
}
