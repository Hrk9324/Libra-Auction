'use client';

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { UserInfo } from "@/types/user_info";
import { NavItem } from "@/types/nav_item";

export default function UserMenuDropdown({ 
  userInfo, 
  authedUserActionItems 
}: { 
  userInfo: UserInfo; 
  authedUserActionItems: NavItem[] 
}) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="relative">
      <div className="h-full flex items-center">
        <Image
          src={userInfo?.anhDaiDien || "/default-avatar.png"}
          alt="Avatar"
          width={40}
          height={40}
          className="rounded-full cursor-pointer shadow-sm w-10 h-10 object-cover border border-gray-200 hover:ring-2 hover:ring-primary-color transition-all"
          onClick={() => setIsOpen(!isOpen)}
        />
      </div>

      {isOpen && (
        <>
          {/* Overlay để click ra ngoài thì đóng menu */}
          <div className="fixed inset-0 z-40" onClick={() => setIsOpen(false)}></div>
          
          <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-100 shadow-2xl rounded-xl py-1 z-50">
            <div className="px-4 py-2 border-b border-gray-50">
              <p className="text-sm font-bold truncate">{userInfo.hoVaTen || "User"}</p>
              <p className="text-xs text-gray-500 truncate">{userInfo.email}</p>
            </div>

            {authedUserActionItems.map(item => (
              <Link
                key={item.value}
                href={item.href}
                className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition"
                onClick={() => setIsOpen(false)}
              >
                {item.value}
              </Link>
            ))}

            <div className="border-t border-gray-100 my-1"></div>

            <Link 
              href="/api/sign-out" 
              className="block px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition"
              onClick={() => setIsOpen(false)}
            >
              Sign out
            </Link>
          </div>
        </>
      )}
    </div>
  );
}