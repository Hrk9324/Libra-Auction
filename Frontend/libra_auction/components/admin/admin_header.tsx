import UserMenu from "@/components/main/user_menu_drop";
import { getIdFromToken } from "@/lib/get_id_from_token";
import { isAuthenticated } from "@/lib/is_authenticated";
import { fetchUserInfo } from "@/services/fetch_user_info";

interface AdminHeaderProps {
  title: string;
  breadcrumb?: Array<{ label: string; href?: string }>;
}

export default async function AdminHeader({ title, breadcrumb }: AdminHeaderProps) {
  const authed = await isAuthenticated();
  const userId = await getIdFromToken();
  let userInfo = null;

  if (authed && userId) {
    userInfo = await fetchUserInfo(userId);
  }

  const authedUserActionItems = [
    { value: "Profile", href: "/profile" },
    { value: "Seller Dashboard", href: "/seller-dashboard" },
    { value: "Auction Lobby", href: "/" },
  ];

  return (
    <header className="h-16 bg-white border-b border-[#AFD3E2] flex items-center justify-between px-6">
      <div className="flex flex-col min-w-0">
        <h1 className="text-xl font-bold text-[#146C94] truncate">{title}</h1>
        {breadcrumb && breadcrumb.length > 0 && (
          <div className="mt-0.5 flex items-center gap-2 text-xs text-[#5A7184] truncate">
            {breadcrumb.map((item, index) => (
              <span key={item.label} className="flex items-center gap-2 min-w-0">
                {index > 0 && <span className="text-[#AFD3E2]">/</span>}
                {item.href ? (
                  <a href={item.href} className="hover:text-[#19A7CE] transition-colors truncate">
                    {item.label}
                  </a>
                ) : (
                  <span className="truncate">{item.label}</span>
                )}
              </span>
            ))}
          </div>
        )}
      </div>

      <div className="flex items-center gap-4">
        {authed && userInfo && (
          <UserMenu userInfo={userInfo} authedUserActionItems={authedUserActionItems} />
        )}
      </div>
    </header>
  );
}
