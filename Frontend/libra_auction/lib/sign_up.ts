export async function signUp(fullName: string, username: string, email: string, password: string, confirmPassword: string, onSuccess: () => void, onFailed: (message: string) => void) {
    try {
        if (password !== confirmPassword) {
            onFailed("Password and Cofirm password not match.");
            return;
        }
        const res = await fetch("/api/sign-up", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fullName: fullName,
                username: username,
                email: email,
                password: password
            })
        })
        const data = await res.json();
        if (!res.ok) {
            onFailed(data.message);
        }
        else if (res.status === 200 || res.status === 201) {
            console.log("Success failed");
            onSuccess();
            console.log("Success failed");
        }
    }
    catch (error) {
        console.log(error);
        onFailed("Internal server error");
    }
}