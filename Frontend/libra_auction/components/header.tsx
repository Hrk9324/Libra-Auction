import Logo from "@/components/logo";
import Nav from "@/components/nav";
import UserAction from "@/components/user_action";

import { NavType } from "@/types/nav_type";
import { UserActionType } from "@/types/user_action_type";
export default function Header() {
  const navItems: NavType[] = [
    { value: "Home", href: "/" },
    { value: "Auction", href: "/auction" },
    { value: "Support", href: "/support" },
    { value: "Contact", href: "/contact" }
  ];
  const userActionItems: UserActionType[] = [
    { value: "Login", href: "/login" },
    { value: "Register", href: "/register" },
  ];
  return (
    <div className="px-8 py-4 flex bg-white shadow-md">
        <Logo />
        <Nav items={navItems} />
        <UserAction items={userActionItems} />
    </div>
  );
}
