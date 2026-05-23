'use client';

import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import logo_img from "@/public/logo.png";

const menuItems = [
  { name: 'Dashboard', href: '/admin-dashboard' },
  { name: 'Users', href: '/admin-dashboard/users' },
  { name: 'Auctions', href: '/admin-dashboard/auctions' },
];

export default function AdminSidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 bg-[#146C94] text-white min-h-screen p-4 flex flex-col">
      <div className="text-2xl font-bold mb-8 p-2">
      <Link href="/" className="flex items-center gap-2 flex-1">
        <Image src={logo_img} alt="Libra Auction" className="h-8 w-8" />
        <span className="text-[1.25rem] font-bold text-white">
          Libra Auction
        </span>
      </Link>
        </div>
      <nav className="flex-1">
        {menuItems.map((item) => {
          const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
          return (
            <Link
              key={item.name}
              href={item.href}
              className={`block p-3 mb-2 rounded transition-colors ${
                isActive
                  ? "bg-[#19A7CE]"
                  : "hover:bg-[#19A7CE]"
              }`}
            >
              {item.name}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
