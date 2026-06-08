'use client';

import { signInAction, signUpAction } from "@/lib/auth_actions";

export async function signInPassword(
    username: string, 
    password: string, 
    onSuccess: () => void, 
    onFailed: (message: string) => void
) {
    const res = await signInAction({ username, password });
    
    if (res.success) {
        onSuccess();
    } else {
        onFailed(res.message);
    }
}

export async function signUp(
    fullName: string, 
    username: string, 
    email: string, 
    password: string, 
    confirmPassword: string, 
    onSuccess: () => void, 
    onFailed: (message: string) => void
) {
    if (password !== confirmPassword) {
        onFailed("Password and Confirm password do not match.");
        return;
    }

    const res = await signUpAction({ fullName, username, email, password });
    
    if (res.success) {
        onSuccess();
    } else {
        onFailed(res.message);
    }
}

export async function signInGoogle(onSuccess: () => void, onFailed: () => void) {
    const w = 500;
    const h = 600;
    const left = (screen.width / 2) - (w / 2);
    const top = (screen.height / 2) - (h / 2);
    
    window.open('/bff/auth/google', "Google login", `width=${w},height=${h},left=${left},top=${top}`);
    
    const handleMessage = (e: MessageEvent) => {
        if (e.data.type === 'AUTH_SUCCESS') {
            onSuccess();
            window.removeEventListener('message', handleMessage);
        }
        else if (e.data.type === 'AUTH_FAILED') {
            onFailed();
            window.removeEventListener('message', handleMessage);
        }
    }
    window.addEventListener('message', handleMessage);
}