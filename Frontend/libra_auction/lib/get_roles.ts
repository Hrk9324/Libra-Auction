 'use server';

import * as jose from "jose";
import { JWSSignatureVerificationFailed, JWTExpired } from "jose/errors";
import { getJWTPublicKey } from "./get_cert";
import { refreshToken } from "./refresh_token";
import { getJWTTokenInfo } from "./get_jwt_token_info";
import { Role } from "@/types/user_info";

function toRoleList(rawRoleValue: unknown): Role[] {
	const roleNames = Array.isArray(rawRoleValue)
		? rawRoleValue
		: typeof rawRoleValue === "string"
			? [rawRoleValue]
			: [];

	return roleNames
		.filter((roleName): roleName is string => typeof roleName === "string" && roleName.length > 0)
		.map((roleName) => ({
			name: roleName,
			description: "",
			permissions: []
		}));
}

export async function getRoles(): Promise<Role[]> {
	const jwtTokenInfo = await getJWTTokenInfo();
	const alg = "RS256";
	const spki = await getJWTPublicKey();

	if (spki && jwtTokenInfo.token) {
		const publicKey = await jose.importSPKI(spki, alg);
		try {
			const { payload } = await jose.jwtVerify(jwtTokenInfo.token, publicKey);
			const roleValue = payload.roles ?? payload.role;
			return toRoleList(roleValue);
		} catch (error) {
			if (error instanceof JWTExpired) {
				console.log("Token expired");
				if (await refreshToken() == false) {
					return [];
				}
				return await getRoles();
			}

			if (error instanceof JWSSignatureVerificationFailed) {
				console.log("Invalid token");
			}
		}
	}

	return [];
}
