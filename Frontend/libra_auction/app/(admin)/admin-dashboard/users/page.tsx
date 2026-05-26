'use client';

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import { fetchPendingUsers } from "@/services/fetch_pending_users";
import { PendingUser } from "@/types/admin/pending_user";

type UserRow = PendingUser & {
  avatar: string;
  accountStatus: string;
  emailStatus: string;
};

type UserFilter = "ALL" | "CHO_XAC_NHAN" | "HOAT_DONG" | "KHOA";

function getStatusBadge(status: string) {
  const statusStyles: Record<string, string> = {
    CHO_XAC_NHAN: "bg-amber-100 text-amber-800 border border-amber-300",
    HOAT_DONG: "bg-green-100 text-green-800 border border-green-300",
    KHOA: "bg-red-100 text-red-800 border border-red-300",
    DA_XAC_THUC: "bg-green-100 text-green-800 border border-green-300",
    CHUA_XAC_THUC: "bg-amber-100 text-amber-800 border border-amber-300",
    CHO_XAC_THUC: "bg-blue-100 text-blue-800 border border-blue-300",
  };

  const labels: Record<string, string> = {
    CHO_XAC_NHAN: "Pending",
    HOAT_DONG: "Active",
    KHOA: "Locked",
    DA_XAC_THUC: "Verified",
    CHUA_XAC_THUC: "Unverified",
    CHO_XAC_THUC: "Awaiting Verification",
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusStyles[status] || "bg-gray-100 text-gray-700 border border-gray-200"}`}>
      {labels[status] || status}
    </span>
  );
}

export default function UsersApprovalPage() {
  const [users, setUsers] = useState<UserRow[]>([]);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<UserFilter>("ALL");

  useEffect(() => {
    const loadUsers = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetchPendingUsers(0, 100);
        setTotalUsers(response.totalElements);
        setUsers(
          response.content.map((user) => ({
            ...user,
            avatar: user.anhDaiDien || "/default-avatar.png",
            accountStatus: user.trangThaiTaiKhoan,
            emailStatus: user.trangThaiEmail,
          })),
        );
      } catch (loadError) {
        setError(loadError instanceof Error ? loadError.message : "Failed to load pending users");
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  const pendingCount = useMemo(() => users.filter((user) => user.accountStatus === "CHO_XAC_NHAN").length, [users]);
  const activeCount = useMemo(() => users.filter((user) => user.accountStatus === "HOAT_DONG").length, [users]);
  const lockedCount = useMemo(() => users.filter((user) => user.accountStatus === "KHOA").length, [users]);

  const filteredUsers = statusFilter === "ALL"
    ? users
    : users.filter((user) => user.accountStatus === statusFilter);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Pending Users</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{totalUsers}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Active Accounts</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{activeCount}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Locked Accounts</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{lockedCount}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
        <h3 className="text-lg font-bold text-[#146C94] mb-4">Filter by Account Status</h3>
        <div className="flex gap-3 flex-wrap">
          <button
            onClick={() => setStatusFilter("ALL")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "ALL"
                ? "bg-[#19A7CE] text-white shadow-sm shadow-[#19A7CE]/30"
                : "bg-[#F6FBFC] text-[#5A7184] hover:bg-[#EAF7FB]"
            }`}
          >
            All
          </button>
          <button
            onClick={() => setStatusFilter("CHO_XAC_NHAN")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "CHO_XAC_NHAN"
                ? "bg-amber-500 text-white"
                : "bg-amber-50 text-amber-800 hover:bg-amber-100"
            }`}
          >
            Pending ({pendingCount})
          </button>
          <button
            onClick={() => setStatusFilter("HOAT_DONG")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "HOAT_DONG"
                ? "bg-green-600 text-white"
                : "bg-green-50 text-green-800 hover:bg-green-100"
            }`}
          >
            Active ({activeCount})
          </button>
          <button
            onClick={() => setStatusFilter("KHOA")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "KHOA"
                ? "bg-red-600 text-white"
                : "bg-red-50 text-red-800 hover:bg-red-100"
            }`}
          >
            Locked ({lockedCount})
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl border border-[#AFD3E2] overflow-hidden shadow-sm shadow-[#AFD3E2]/20">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-[#AFD3E2] bg-[#F6FBFC]">
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Avatar</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Full Name</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Email</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Phone</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">CCCD</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Email Status</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Account Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td className="px-6 py-8 text-center text-[#5A7184]" colSpan={7}>
                    Loading...
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td className="px-6 py-8 text-center text-[#5A7184]" colSpan={7}>
                    No pending users available
                  </td>
                </tr>
              ) : (
                filteredUsers.map((row) => (
                  <tr key={row.id} className="border-b border-[#EAF3F6] hover:bg-[#F8FCFD]">
                    <td className="px-6 py-4">
                      <Image
                        src={row.avatar}
                        alt={row.hoVaTen}
                        width={60}
                        height={60}
                        className="w-16 h-16 rounded-lg object-cover"
                        onError={(e) => {
                          e.currentTarget.src = "/default-avatar.png";
                        }}
                      />
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-[#5A7184]">{row.hoVaTen}</td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.email}</td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.soDienThoai || "-"}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-[#19A7CE]">{row.CCCD || "-"}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.emailStatus)}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.accountStatus)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}