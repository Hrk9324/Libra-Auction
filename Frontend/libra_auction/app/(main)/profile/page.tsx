import { getErrorStatus } from "@/lib/app_error";
import { UserDashboard } from "@/components/main/profile/UserDashboard";
import { getIdFromToken } from "@/lib/get_id_from_token";
import { fetchUserInfo } from "@/services/fetch_user_info";
import { redirect } from "next/navigation";
import { Suspense } from "react";

function DashboardFallback() {
  return (
    <div className="animate-pulse space-y-6">
      <div className="h-10 bg-gray-200 rounded w-1/3"></div>
      <div className="h-12 bg-gray-200 rounded"></div>
      <div className="h-64 bg-gray-200 rounded"></div>
    </div>
  );
}

export default async function ProfilePage() {
  const user_id = await getIdFromToken();

  if (!user_id) {
    redirect("/sign-in");
  }

  let user;
  try {
    user = await fetchUserInfo(user_id);
  } catch (error) {
    if (getErrorStatus(error) === 401) redirect("/sign-in");
    throw error;
  }

  return (
    <Suspense fallback={<DashboardFallback />}>
      {user && <UserDashboard user={user} />}
    </Suspense>
  );
}
