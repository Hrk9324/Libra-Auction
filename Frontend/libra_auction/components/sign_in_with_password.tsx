"use client";
import { FormEvent, useState } from "react";
import PasswordInput from "./passwordInput";
import { signInPassword } from "@/lib/sign_in";

export default function SignInWithPassword() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const handleSignIn = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    const onSuccess = () => {
      window.location.replace("/");
    };
    const onFailed = (message: string) => {
      setErrorMessage(message);
    };
    await signInPassword(username, password, onSuccess, onFailed);
  };
  return (
    <form onSubmit={handleSignIn}>
      <div className="flex flex-col mt-6 gap-4">
        <input
          type="text"
          placeholder="Username"
          className="bg-white border rounded-sm px-4 py-4 focus:outline-(--primary-color)"
          value={username}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setUsername(inputElement.value);
          }}
        />
        <PasswordInput
          name="password"
          placeholder="Password"
          value={password}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setPassword(inputElement.value);
          }}
        />
        {errorMessage && <div className="text-red-700">{errorMessage}</div>}
        <input
          type="submit"
          value="Sign in"
          className="bg-(--primary-color) text-white font-bold text-lg rounded-full p-3 cursor-pointer hover:bg-(--primary-color)/90 active:bg-(--primary-color)/80"
        />
      </div>
    </form>
  );
}
