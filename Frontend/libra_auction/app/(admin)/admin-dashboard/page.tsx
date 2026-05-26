'use client';

import { useState, useEffect } from "react";
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
        console.error('Error fetching pending data:', error);
      }
    };

    fetchPendingData();
  }, []);

  return (
    <div className="space-y-8">
      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Revenue Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">Total Revenue</p>
          <p className="text-2xl font-bold text-gray-800 mt-1">45.2M</p>
        </div>

        {/* Total Transactions Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">Total Transactions</p>
          <p className="text-2xl font-bold text-gray-800 mt-1">2,547</p>
        </div>

        {/* Successful Auctions Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">Successful Auctions</p>
          <p className="text-2xl font-bold text-gray-800 mt-1">1,823</p>
        </div>

        {/* New Users Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">New Users</p>
          <p className="text-2xl font-bold text-gray-800 mt-1">342</p>
        </div>
      </div>

      {/* Pending Approvals Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Pending Users Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">Pending Users</p>
          <p className="text-2xl font-bold text-orange-600 mt-1">{pendingData.pendingUsers}</p>
        </div>

        {/* Pending Auctions Card */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-5">
          <p className="text-xs font-semibold text-gray-600 uppercase">Pending Auctions</p>
          <p className="text-2xl font-bold text-orange-600 mt-1">{pendingData.pendingAuctions}</p>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-[#AFD3E2] p-6">
        <h3 className="text-lg font-bold text-[#146C94] mb-2">Admin Overview</h3>
        <p className="text-sm text-gray-600">
          This overview now reflects only live backend approval queues.
        </p>
      </div>
    </div>
  );
}