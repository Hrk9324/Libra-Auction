import Image from "next/image";
import Link from "next/link";
import { isAuthenticated } from "@/lib/is_authenticated";
import { fetchUserInfo } from "@/services/fetch_user_info";
import logo_img from "@/public/logo.png";
import UserMenuDropdown from "./user_menu_drop";
import { getIdFromToken } from "@/lib/get_id_from_token";

export default async function Header() {
  const authed = await isAuthenticated();
  const userId = await getIdFromToken();
  let userInfo = null;
  if (authed && userId) {
    userInfo = await fetchUserInfo(userId);
  }

  const navItems = [
    { value: "Categories", href: "/categories" },
    { value: "Auctions", href: "/auctions" },
    { value: "Support", href: "/support" },
    { value: "Contact", href: "/contact" },
  ];

  const userActionItems = [
    { value: "Sign in", href: "/sign-in" },
    { value: "Sign up", href: "/sign-up" },
  ];

  const authedUserActionItems = [
    { value: "Profile", href: "/profile" },
    ...(userInfo?.role?.name === "ADMIN"
      ? [{ value: "Admin Dashboard", href: "/admin-dashboard" }]
      : []),
    { value: "Seller Dashboard", href: "/seller-dashboard" },
  ];

  return (
    <header className="px-8 py-4 flex items-center bg-white shadow-md sticky top-0 z-30">
      {/* --- LOGO --- */}
      <Link href="/" className="flex items-center gap-2 flex-1">
        <Image src={logo_img} alt="Libra Auction" className="h-8 w-8" />
        <span className="text-[1.25rem] font-bold text-(--primary-color)">
          Libra Auction
        </span>
      </Link>

      {/* --- NAVIGATION --- */}
      <nav className="hidden md:block flex-2">
        <ul className="flex gap-10 justify-center items-center font-semibold text-gray-600">
          {navItems.map((item) => (
            <li key={item.value}>
              <Link href={item.href} className="hover:text-(--primary-color) transition">
                {item.value}
              </Link>
            </li>
          ))}
        </ul>
      </nav>

      {/* --- ACTIONS / USER MENU --- */}
      <div className="flex items-center justify-end flex-1">
        {!authed ? (
          <div className="flex gap-4 items-center">
            {userActionItems.map((item) => (
              <Link
                key={item.value}
                href={item.href}
                className={item.value === "Sign up"
                  ? "px-5 py-2 rounded-full bg-(--primary-color) text-white hover:opacity-90 transition"
                  : "font-medium hover:text-(--primary-color) transition"}
              >
                {item.value}
              </Link>
            ))}
          </div>
        ) : (
          userInfo && (
            <UserMenuDropdown
              userInfo={userInfo}
              authedUserActionItems={authedUserActionItems}
            />
          )
        )}
      </div>
    </header>
  );
}