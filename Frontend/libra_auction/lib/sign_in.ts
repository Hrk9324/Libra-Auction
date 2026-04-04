export async function signInPassword(username: string, password: string, onSuccess: () => void, onFailed: (message: string) => void) {
    const res = await fetch('/api/auth/password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username, password
        })
    });
    const data = await res.json();
    if (!res.ok) {
        onFailed(data.message);
    }
    else {
        onSuccess();
    }
}

export async function signInGoogle(onSuccess: () => void, onFailed: () => void) {
    const w = 500;
    const h = 600;
    const left = (screen.width / 2) - (w / 2);
    const top = (screen.height / 2) - (h / 2);
    window.open('/api/auth/google', "Google login", `width=${w},height=${h},left=${left},top=${top}`);
    const handleMessage = (e: MessageEvent) => {
        if (e.data.type === 'AUTH_SUCCESS') {
            onSuccess();
            window.removeEventListener('message', handleMessage);
        }
        else if (e.data.type === 'AUTH_FAILED') {
            onFailed();
        }
    }
    window.addEventListener('message', handleMessage);
}