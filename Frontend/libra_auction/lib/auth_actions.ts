'use server';

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { ServerAPICall } from "@/lib/server_API_call";
import { JWTResponse } from "@/types/jwt_response";
import { clearAuthCookies } from "@/lib/clear_auth_cookies";

interface ActionResponse {
    success: boolean;
    message: string;
}

export async function signInAction(body: any): Promise<ActionResponse> {
    try {
        const { username, password } = body;
        
        const res = await ServerAPICall<JWTResponse>("/auth/signin", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (res.isSuccess && res.data) {
            const cookieStore = await cookies();
            
            cookieStore.set({
                name: 'jwtToken',
                value: res.data.token,
                httpOnly: true,
                secure: true,
                maxAge: Math.floor(res.data.accessTokenExpiration / 1000)
            });
            
            cookieStore.set({
                name: 'refreshToken',
                value: res.data.refreshToken,
                httpOnly: true,
                secure: true,
                maxAge: Math.floor(res.data.refreshTokenExpiration / 1000)
            });

            return { success: true, message: "Sign in successful" };
        }
        
        return { success: false, message: res.errorMessage || "Failed to sign in" };
    } catch (e) {
        console.error("Error in signInAction:", e);
        return { success: false, message: "Internal server error" };
    }
}

export async function signUpAction(body: any): Promise<ActionResponse> {
    try {
        const { fullName, username, email, password } = body;
        
        const res = await fetch(process.env.BACKEND_SERVER_URL + '/auth/signup', {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, username, email, password })
        });

        const data = await res.json();
        
        if (!res.ok) {
            return { success: false, message: data.message || "Sign up failed" };
        }
        
        return { success: true, message: "Sign up success" };
    } catch (error) {
        console.error("Error in signUpAction:", error);
        return { success: false, message: "Internal server error" };
    }
}

export async function signOutAction(): Promise<void> {
    await clearAuthCookies();
    redirect('/sign-in');
}