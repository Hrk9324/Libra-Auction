"use client";
import { FormEvent, useState } from "react";
import PasswordInput from "./passwordInput";
import { signUp } from "@/lib/sign_up";

export default function SignUpSection() {
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const handleSignUp = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    const onSuccess = () => {
      window.location.replace("/sign-in");
    };
    const onFailed = (message: string) => {
      setErrorMessage(message);
    };
    await signUp(
      fullName,
      username,
      email,
      password,
      confirmPassword,
      onSuccess,
      onFailed,
    );
  };
  return (
    <form onSubmit={handleSignUp}>
      <div className="flex flex-col mt-6 gap-4">
        <input
          type="text"
          placeholder="Full Name"
          value={fullName}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setFullName(inputElement.value);
          }}
          className="bg-white border rounded-sm px-4 py-4 focus:outline-(--primary-color)"
        />
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setUsername(inputElement.value);
          }}
          className="bg-white border rounded-sm px-4 py-4 focus:outline-(--primary-color)"
        />
        <input
          type="text"
          placeholder="Email"
          value={email}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setEmail(inputElement.value);
          }}
          className="bg-white border rounded-sm px-4 py-4 focus:outline-(--primary-color)"
        />{" "}
        <PasswordInput
          name="password"
          placeholder="Password"
          value={password}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setPassword(inputElement.value);
          }}
        />
        <PasswordInput
          name="confirm-password"
          placeholder="Confirm password"
          value={confirmPassword}
          onChange={(e: FormEvent) => {
            const inputElement = e.target as HTMLInputElement;
            setConfirmPassword(inputElement.value);
          }}
        />
        {errorMessage && <div className="text-red-700">{errorMessage}</div>}
        <input
          type="submit"
          value="Sign up"
          className="bg-(--primary-color) text-white font-bold text-lg rounded-full p-3 cursor-pointer hover:bg-(--primary-color)/90 active:bg-(--primary-color)/80"
        />
      </div>
    </form>
  );
}
