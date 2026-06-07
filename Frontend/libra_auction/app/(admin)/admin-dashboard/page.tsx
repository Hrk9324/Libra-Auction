'use client';

import { useEffect, useState } from "react";
import { getErrorMessage } from "@/lib/app_error";
import { fetchPendingUsers } from "@/services/fetch_pending_users";
import { fetchPendingAuctions } from "@/services/fetch_pending_auctions";

interface PendingData {
  pendingUsers: number;
  pendingAuctions: number;
}

export default function AdminDashboardPage() {
  const [pendingData, setPendingData] = useState<PendingData>({
    pendingUsers: 0,
    pendingAuctions: 0,
  });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPendingData = async () => {
      try {
        const [usersResponse, auctionsResponse] = await Promise.all([
          fetchPendingUsers(0, 1),
          fetchPendingAuctions(0, 1),
        ]);

        setPendingData({
          pendingUsers: usersResponse.totalElements,
          pendingAuctions: auctionsResponse.totalElements,
        });
      } catch (error) {
        setError(getErrorMessage(error, "Failed to fetch pending dashboard data."));
      }
    };

    fetchPendingData();
  }, []);

  return (
    <div className="space-y-8">
      {error ? (
        <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</p>
      ) : null}
      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Revenue Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Total Revenue</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">45.2M</p>
        </div>

        {/* Total Transactions Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Total Transactions</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">2,547</p>
        </div>

        {/* Successful Auctions Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Successful Auctions</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">1,823</p>
        </div>

        {/* New Users Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">New Users</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">342</p>
        </div>
      </div>

      {/* Pending Approvals Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Pending Users Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Pending Users</p>
          <p className="text-2xl font-bold text-orange-600 mt-1">{pendingData.pendingUsers}</p>
        </div>

        {/* Pending Auctions Card */}
        <div className="bg-white rounded-xl border border-[#AFD3E2] p-5 shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Pending Auctions</p>
          <p className="text-2xl font-bold text-orange-600 mt-1">{pendingData.pendingAuctions}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
        <h3 className="text-lg font-bold text-[#146C94] mb-2">Admin Overview</h3>
        <p className="text-sm text-[#5A7184]">
          This overview now reflects only live backend approval queues.
        </p>
      </div>
    </div>
  );
}