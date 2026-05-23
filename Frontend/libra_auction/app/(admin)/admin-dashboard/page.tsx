'use client';

import { useState } from "react";

// Mock Chart Data
const mockChartData = [
  { date: "May 1", revenue: 2500 },
  { date: "May 2", revenue: 3200 },
  { date: "May 3", revenue: 2800 },
  { date: "May 4", revenue: 3900 },
  { date: "May 5", revenue: 4200 },
  { date: "May 6", revenue: 3800 },
  { date: "May 7", revenue: 4500 },
  { date: "May 8", revenue: 5200 },
  { date: "May 9", revenue: 4800 },
  { date: "May 10", revenue: 5600 },
  { date: "May 11", revenue: 6200 },
  { date: "May 12", revenue: 5900 },
  { date: "May 13", revenue: 6500 },
  { date: "May 14", revenue: 7100 },
];

export default function AdminDashboardPage() {
  const [timeRange, setTimeRange] = useState<"day" | "month" | "year">("month");

  // Calculate max revenue for scaling
  const maxRevenue = Math.max(...mockChartData.map((d) => d.revenue));

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

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Revenue Chart */}
        <div className="lg:col-span-2 bg-white rounded-lg border border-[#AFD3E2] p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-lg font-bold text-[#146C94]">Revenue Over Time</h3>
            <div className="flex gap-2">
              {(["day", "month", "year"] as const).map((range) => (
                <button
                  key={range}
                  onClick={() => setTimeRange(range)}
                  className={`px-3 py-1 rounded text-xs font-semibold transition ${
                    timeRange === range
                      ? "bg-[#19A7CE] text-white"
                      : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                  }`}
                >
                  {range.charAt(0).toUpperCase() + range.slice(1)}
                </button>
              ))}
            </div>
          </div>

          {/* Simple Bar Chart using divs */}
          <div className="space-y-3">
            <div className="flex items-end justify-between gap-1 h-48">
              {mockChartData.map((data, idx) => (
                <div key={idx} className="flex-1 flex flex-col items-center">
                  <div
                    className="w-full bg-gradient-to-t from-[#19A7CE] to-[#0f7a99] rounded-t hover:from-[#1589ab] transition-colors group"
                    style={{
                      height: `${(data.revenue / maxRevenue) * 100}%`,
                      minHeight: "4px",
                    }}
                    title={`${data.date}: ${data.revenue.toLocaleString()}`}
                  >
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity text-xs font-semibold text-white text-center mt-1 absolute translate-y-8 pointer-events-none">
                      {data.revenue}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* X-axis Labels */}
            <div className="flex justify-between text-xs text-gray-500 mt-2">
              {mockChartData.map((data, idx) => (
                <span key={idx}>{data.date}</span>
              ))}
            </div>
          </div>
        </div>

        {/* Top Metrics */}
        <div className="bg-white rounded-lg border border-[#AFD3E2] p-6 space-y-4">
          <h3 className="text-lg font-bold text-[#146C94] mb-4">Top Metrics</h3>

          <div className="space-y-4">
            {[
              { label: "Pending Approvals", value: 12, color: "text-orange-600" },
              { label: "Active Auctions", value: 156, color: "text-green-600" },
              { label: "Flagged Items", value: 8, color: "text-red-600" },
              { label: "Avg Transaction Value", value: "17.8M", color: "text-blue-600" },
            ].map((metric, idx) => (
              <div key={idx} className="flex items-center justify-between">
                <p className="text-sm text-gray-700">{metric.label}</p>
                <p className={`text-lg font-bold ${metric.color}`}>{metric.value}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-white rounded-lg border border-[#AFD3E2] p-6">
        <h3 className="text-lg font-bold text-[#146C94] mb-4">Recent Activity</h3>
        <div className="space-y-3">
          {[
            { event: "New user registered", time: "2 minutes ago", type: "info" },
            { event: "Auction approved", time: "15 minutes ago", type: "success" },
            { event: "User flagged for review", time: "45 minutes ago", type: "warning" },
            { event: "Transaction completed", time: "1 hour ago", type: "success" },
          ].map((activity, idx) => {
            const typeColors = {
              info: "bg-blue-100 text-blue-800",
              success: "bg-green-100 text-green-800",
              warning: "bg-orange-100 text-orange-800",
            };
            return (
              <div key={idx} className="flex items-center gap-4 pb-3 border-b border-gray-100 last:border-0">
                <div className={`w-3 h-3 rounded-full ${typeColors[activity.type as keyof typeof typeColors]}`} />
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-800">{activity.event}</p>
                  <p className="text-xs text-gray-500">{activity.time}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}