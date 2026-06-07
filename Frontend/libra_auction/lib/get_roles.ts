'use server';

import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { Role } from "@/types/user_info";
import { clearAuthCookies } from "./clear_auth_cookies";
import { getJWTPublicKey } from "./get_cert";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { refreshToken } from "./refresh_token";

function toRole(rawRoleValue: unknown): Role | null {
	if (typeof rawRoleValue === "string" && rawRoleValue.length > 0) {
		return { name: rawRoleValue, description: "", permissions: [] };
	}

	if (Array.isArray(rawRoleValue)) {
		const first = rawRoleValue.find((role): role is string => typeof role === "string" && role.length > 0);
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
			await clearAuthCookies();
		}
	}

	return null;
}
