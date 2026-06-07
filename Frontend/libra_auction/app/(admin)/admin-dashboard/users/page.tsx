'use client';

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import AdminModal from "@/components/admin/admin_modal";
import { fetchAdminUsers, updateAdminUserAction } from "@/services/fetch_pending_users";
import { PendingUser } from "@/types/admin/pending_user";

type UserRow = PendingUser & {
  avatar: string;
  accountStatus: string;
  emailStatus: string;
};

type AccountStatusFilter = "ALL" | "PENDING" | "ACTIVE" | "LOCKED";
type EmailStatusFilter = "ALL" | "VERIFIED" | "UNVERIFIED" | "PENDING_VERIFICATION";
type UserAction = "approve" | "reject" | "lock" | "unlock";

type UserFilters = {
  searchName: string;
  email: string;
  phone: string;
  identityNumber: string;
  emailStatus: EmailStatusFilter;
  accountStatus: AccountStatusFilter;
};

const defaultFilters: UserFilters = {
  searchName: "",
  email: "",
  phone: "",
  identityNumber: "",
  emailStatus: "ALL",
  accountStatus: "ALL",
};

function mapUser(user: PendingUser): UserRow {
  return {
    ...user,
    avatar: user.avatarUrl || "/default-avatar.png",
    accountStatus: user.accountStatus,
    emailStatus: user.emailStatus,
  };
}

function getStatusBadge(status: string) {
  const statusStyles: Record<string, string> = {
    PENDING: "bg-amber-100 text-amber-800 border border-amber-300",
    ACTIVE: "bg-green-100 text-green-800 border border-green-300",
    LOCKED: "bg-red-100 text-red-800 border border-red-300",
    VERIFIED: "bg-green-100 text-green-800 border border-green-300",
    UNVERIFIED: "bg-amber-100 text-amber-800 border border-amber-300",
    PENDING_VERIFICATION: "bg-blue-100 text-blue-800 border border-blue-300",
  };

  const labels: Record<string, string> = {
    PENDING: "Pending",
    ACTIVE: "Active",
    LOCKED: "Locked",
    VERIFIED: "Verified",
    UNVERIFIED: "Unverified",
    PENDING_VERIFICATION: "Awaiting Verification",
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusStyles[status] || "bg-gray-100 text-gray-700 border border-gray-200"}`}>
      {labels[status] || status}
    </span>
  );
}

export default function UsersApprovalPage() {
  const [users, setUsers] = useState<UserRow[]>([]);
  const [selectedUser, setSelectedUser] = useState<{ isOpen: boolean; data: UserRow | null }>({
    isOpen: false,
    data: null,
  });
  const [filters, setFilters] = useState<UserFilters>(defaultFilters);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  useEffect(() => {
    const loadUsers = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetchAdminUsers(0, 1000);
        setUsers(response.content.map(mapUser));
      } catch (loadError) {
        setError(loadError instanceof Error ? loadError.message : "Failed to load users");
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  const pendingCount = useMemo(() => users.filter((user) => user.accountStatus === "PENDING").length, [users]);
  const activeCount = useMemo(() => users.filter((user) => user.accountStatus === "ACTIVE").length, [users]);
  const lockedCount = useMemo(() => users.filter((user) => user.accountStatus === "LOCKED").length, [users]);

  const filteredUsers = useMemo(() => {
    const normalizedName = filters.searchName.trim().toLowerCase();
    const normalizedEmail = filters.email.trim().toLowerCase();
    const normalizedPhone = filters.phone.trim().toLowerCase();
    const normalizedIdentityNumber = filters.identityNumber.trim().toLowerCase();

    return users.filter((user) => (
      (!normalizedName || user.fullName.toLowerCase().includes(normalizedName)) &&
      (!normalizedEmail || user.email.toLowerCase().includes(normalizedEmail)) &&
      (!normalizedPhone || (user.phoneNumber || "").toLowerCase().includes(normalizedPhone)) &&
      (!normalizedIdentityNumber || (user.identityNumber || "").toLowerCase().includes(normalizedIdentityNumber)) &&
      (filters.emailStatus === "ALL" || user.emailStatus === filters.emailStatus) &&
      (filters.accountStatus === "ALL" || user.accountStatus === filters.accountStatus)
    ));
  }, [users, filters]);

  const handleAction = async (user: UserRow, action: UserAction) => {
    try {
      setActionLoadingId(user.id);
      setError(null);
      const updatedUser = mapUser(await updateAdminUserAction(user.id, action));
      setUsers((current) => current.map((item) => (item.id === updatedUser.id ? updatedUser : item)));
      setSelectedUser((current) => ({
        ...current,
        data: current.data?.id === updatedUser.id ? updatedUser : current.data,
      }));
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : `Failed to ${action} user`);
    } finally {
      setActionLoadingId(null);
    }
  };

  const renderActions = (row: UserRow) => (
    <div className="flex w-32 flex-col gap-2">
      <button
        onClick={() => setSelectedUser({ isOpen: true, data: row })}
        className="w-full px-3 py-1.5 bg-[#EAF7FB] text-[#146C94] rounded hover:bg-[#D7EFF7] text-xs font-semibold text-center"
      >
        Details
      </button>

      {row.accountStatus === "PENDING" && (
        <>
          <button
            onClick={() => handleAction(row, "approve")}
            disabled={actionLoadingId === row.id}
            className="w-full px-3 py-1.5 bg-green-50 text-green-700 rounded hover:bg-green-100 text-xs font-semibold text-center disabled:cursor-not-allowed disabled:opacity-50"
          >
            Approve
          </button>
          <button
            onClick={() => handleAction(row, "reject")}
            disabled={actionLoadingId === row.id}
            className="w-full px-3 py-1.5 bg-red-50 text-red-700 rounded hover:bg-red-100 text-xs font-semibold text-center disabled:cursor-not-allowed disabled:opacity-50"
          >
            Reject
          </button>
        </>
      )}

      {row.accountStatus === "ACTIVE" && (
        <button
          onClick={() => handleAction(row, "lock")}
          disabled={actionLoadingId === row.id}
          className="w-full px-3 py-1.5 bg-rose-50 text-rose-700 rounded hover:bg-rose-100 text-xs font-semibold text-center disabled:cursor-not-allowed disabled:opacity-50"
        >
          Lock
        </button>
      )}

      {row.accountStatus === "LOCKED" && (
        <button
          onClick={() => handleAction(row, "unlock")}
          disabled={actionLoadingId === row.id}
          className="w-full px-3 py-1.5 bg-emerald-50 text-emerald-700 rounded hover:bg-emerald-100 text-xs font-semibold text-center disabled:cursor-not-allowed disabled:opacity-50"
        >
          Unlock
        </button>
      )}
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Pending</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{pendingCount}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Active</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{activeCount}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Locked</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{lockedCount}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
        <div className="mb-5 flex items-center justify-between gap-3">
          <div>
            <h3 className="text-lg font-bold text-[#146C94]">User Filters</h3>
            <p className="text-sm text-[#5A7184]">Filter by name, email, phone, identity number, email status and account status.</p>
          </div>
          <button
            type="button"
            onClick={() => setFilters(defaultFilters)}
            className="rounded-lg bg-[#EAF7FB] px-4 py-2 text-sm font-semibold text-[#146C94] hover:bg-[#D7EFF7]"
          >
            Reset
          </button>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Full name
            <input
              type="search"
              value={filters.searchName}
              onChange={(event) => setFilters((current) => ({ ...current, searchName: event.target.value }))}
              placeholder="Search by name"
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            />
          </label>

          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Email
            <input
              type="search"
              value={filters.email}
              onChange={(event) => setFilters((current) => ({ ...current, email: event.target.value }))}
              placeholder="Search by email"
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            />
          </label>

          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Phone
            <input
              type="search"
              value={filters.phone}
              onChange={(event) => setFilters((current) => ({ ...current, phone: event.target.value }))}
              placeholder="Search by phone"
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            />
          </label>

          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Identity number
            <input
              type="search"
              value={filters.identityNumber}
              onChange={(event) => setFilters((current) => ({ ...current, identityNumber: event.target.value }))}
              placeholder="Search by identity number"
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            />
          </label>

          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Email status
            <select
              value={filters.emailStatus}
              onChange={(event) => setFilters((current) => ({ ...current, emailStatus: event.target.value as EmailStatusFilter }))}
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            >
              <option value="ALL">All email statuses</option>
              <option value="VERIFIED">Verified</option>
              <option value="UNVERIFIED">Unverified</option>
              <option value="PENDING_VERIFICATION">Awaiting Verification</option>
            </select>
          </label>

          <label className="space-y-1.5 text-sm font-semibold text-[#146C94]">
            Account status
            <select
              value={filters.accountStatus}
              onChange={(event) => setFilters((current) => ({ ...current, accountStatus: event.target.value as AccountStatusFilter }))}
              className="w-full rounded-lg border border-[#AFD3E2] bg-white px-3 py-2 text-sm font-normal text-[#5A7184] outline-none focus:border-[#19A7CE]"
            >
              <option value="ALL">All account statuses</option>
              <option value="PENDING">Pending</option>
              <option value="ACTIVE">Active</option>
              <option value="LOCKED">Locked</option>
            </select>
          </label>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl border border-[#AFD3E2] overflow-hidden shadow-sm shadow-[#AFD3E2]/20">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-[#AFD3E2] bg-[#F6FBFC]">
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Avatar</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">User</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Phone</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Identity Number</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Email Status</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Account Status</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Actions</th>
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
                    No users available
                  </td>
                </tr>
              ) : (
                filteredUsers.map((row) => (
                  <tr key={row.id} className="border-b border-[#EAF3F6] hover:bg-[#F8FCFD]">
                    <td className="px-6 py-4">
                      <Image
                        src={row.avatar}
                        alt={row.fullName}
                        width={60}
                        height={60}
                        className="aspect-square w-16 rounded-lg object-cover"
                        onError={(e) => {
                          e.currentTarget.src = "/default-avatar.png";
                        }}
                      />
                    </td>
                    <td className="px-6 py-4">
                      <p className="text-sm font-medium text-[#5A7184]">{row.fullName}</p>
                      <p className="text-xs text-gray-400 truncate max-w-xs">{row.email}</p>
                    </td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.phoneNumber || "-"}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-[#19A7CE]">{row.identityNumber || "-"}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.emailStatus)}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.accountStatus)}</td>
                    <td className="px-6 py-4 text-sm align-top">{renderActions(row)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedUser.data && (
        <AdminModal
          isOpen={selectedUser.isOpen}
          onClose={() => setSelectedUser({ isOpen: false, data: null })}
          title="User Detail"
          size="large"
        >
          <div className="grid gap-6 md:grid-cols-[180px_1fr]">
            <Image
              src={selectedUser.data.avatar}
              alt={selectedUser.data.fullName}
              width={180}
              height={180}
              className="aspect-square w-44 rounded-xl border border-[#AFD3E2] object-cover"
              onError={(e) => {
                e.currentTarget.src = "/default-avatar.png";
              }}
            />
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Full name</p>
                <p className="mt-1 text-sm font-medium text-[#146C94]">{selectedUser.data.fullName}</p>
              </div>
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Email</p>
                <p className="mt-1 text-sm font-medium text-[#146C94]">{selectedUser.data.email}</p>
              </div>
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Phone</p>
                <p className="mt-1 text-sm font-medium text-[#146C94]">{selectedUser.data.phoneNumber || "-"}</p>
              </div>
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Identity number</p>
                <p className="mt-1 text-sm font-medium text-[#146C94]">{selectedUser.data.identityNumber || "-"}</p>
              </div>
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Email status</p>
                <div className="mt-2">{getStatusBadge(selectedUser.data.emailStatus)}</div>
              </div>
              <div>
                <p className="text-xs font-semibold text-[#5A7184] uppercase">Account status</p>
                <div className="mt-2">{getStatusBadge(selectedUser.data.accountStatus)}</div>
              </div>
            </div>
          </div>
        </AdminModal>
      )}
    </div>
  );
}
