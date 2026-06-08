'use client';

import { signUpAction } from "@/lib/auth_actions";

export async function signUp(
    fullName: string, 
    username: string, 
    email: string, 
    password: string, 
    confirmPassword: string, 
    onSuccess: () => void, 
    onFailed: (message: string) => void
) {
    try {
        if (password !== confirmPassword) {
            onFailed("Password and Confirm password do not match.");
            return;
        }

        const res = await signUpAction({
            fullName,
            username,
            email,
            password
        });

        if (!res.success) {
            onFailed(res.message);
        } else {
            onSuccess();
        }
    }
    catch (error) {
        console.error("Client signup error:", error);
        onFailed("Internal server error");
    }
}