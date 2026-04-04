"use client";
import GoogleLogo from "@/public/google-logo.svg";
import Image from "next/image";
import { Roboto } from "next/font/google";
import { signInGoogle } from "@/lib/sign_in";

const roboto = Roboto({
  subsets: ["latin"],
  weight: ["400", "500"],
});
export default function GoogleSignInButton() {
  const handleGoogleSignIn = () => {
    const onSuccess = () => {
      window.location.replace("/");
    };
    const onFailed = () => {
    };
    signInGoogle(onSuccess, onFailed);
  };
  return (
    <button
      className="bg-white border border-[#747775] rounded-full cursor-pointer p-3 w-full hover:bg-gray-100 active:bg-gray-200"
      onClick={handleGoogleSignIn}
    >
      <div className="flex items-center justify-center">
        <Image src={GoogleLogo} alt="" className="h-6" />
        <p className={`${roboto.className} font-medium text-lg`}>
          Sign in with Google
        </p>
      </div>
    </button>
  );
}
