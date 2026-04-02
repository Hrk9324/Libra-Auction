import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
    try {
        const body = await request.json();
        const { fullName, username, email, password } = body;
        const res = await fetch(process.env.BACKEND_SERVER_URL! + '/identity/signup', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                'fullName': fullName,
                'username': username,
                'email': email,
                'password': password,
            })
        });
        const data = await res.json();
        console.log(data);
        console.log(res.status);
        if (!res.ok) {
            return NextResponse.json({ message: data.message || "Sign up failed" }, { status: res.status })
        }
        else if (res.status === 200) {
            return NextResponse.json({ status: 200 });
        }
    }
    catch (error) {
        return NextResponse.json({ message: "Internal server error" }, { status: 500 })
    }
    return NextResponse.json({ message: "Internal server error" }, { status: 500 })
}