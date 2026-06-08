import UserMenu from "@/components/main/user_menu_drop";
import { getIdFromToken } from "@/lib/get_id_from_token";
import { isAuthenticated } from "@/lib/is_authenticated";
import { fetchUserInfo } from "@/services/fetch_user_info";

export default async function Header() {
  const authed = await isAuthenticated();
  const user_id = await getIdFromToken();
  if(!(authed && user_id)) return null;
  const userInfo = await fetchUserInfo(user_id);
  const authedUserActionItems = [
    { value: "Profile", href: "/profile" },
    ...(userInfo.role?.name === "ADMIN"
      ? [{ value: "Admin Dashboard", href: "/admin-dashboard" }]
      : []),
    { value: "Auction Lobby", href: "/" },
  ];
  return (
    <header className="h-16 bg-white border-b border-[#AFD3E2] flex items-center justify-between px-6">
      <h1 className="text-xl font-bold text-[#146C94]">Seller Dashboard</h1>
      <div className="flex items-center gap-4">
        {authed && userInfo && (
          <UserMenu userInfo={userInfo} authedUserActionItems={authedUserActionItems} />
        )}
      </div>
    </header>
  );
}