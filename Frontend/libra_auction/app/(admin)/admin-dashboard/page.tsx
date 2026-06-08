import { fetchAdminDashboardStats } from "@/services/fetch_admin_dashboard_stats";
import type { DashboardAuctionSummary, DashboardRevenuePoint, DashboardStatusCount } from "@/types/dashboard_stats";

const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

const numberFormatter = new Intl.NumberFormat("vi-VN");

function formatCurrency(value: number): string {
  return currencyFormatter.format(value);
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit" }).format(new Date(value));
}

function statusLabel(status: string): string {
  return status.replaceAll("_", " ").toLowerCase();
}

function badgeClassName(status: string): string {
  if (status === "IN_PROGRESS") return "bg-red-50 text-red-700 ring-red-200";
  if (status === "NOT_STARTED" || status === "ACTIVE" || status === "APPROVED") return "bg-emerald-50 text-emerald-700 ring-emerald-200";
  if (status === "COMPLETED") return "bg-[#EAF7FB] text-[#146C94] ring-[#AFD3E2]";
  if (status === "LOCKED" || status === "FAILED" || status === "REJECTED") return "bg-slate-100 text-slate-600 ring-slate-200";
  return "bg-orange-50 text-orange-700 ring-orange-200";
}

function StatCard({ label, value, note, tone }: { label: string; value: string | number; note: string; tone: string }) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-[#AFD3E2]/60 bg-white p-6 shadow-xs transition-all duration-300 hover:-translate-y-0.5 hover:shadow-md hover:shadow-[#19A7CE]/5">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[#5A7184]">{label}</p>
      <div className="mt-2 flex items-baseline gap-2">
        <p className={`text-3xl font-extrabold tracking-tight ${tone}`}>{value}</p>
      </div>
      <p className="mt-1 text-xs text-[#5A7184]/80">{note}</p>
      <div className="absolute inset-x-0 bottom-0 h-0.5 bg-linear-to-r from-transparent via-[#19A7CE]/20 to-transparent opacity-0 transition-opacity group-hover:opacity-100" />
    </div>
  );
}

function RevenueChart({ points }: { points: DashboardRevenuePoint[] }) {
  const totalRevenue = points.reduce((sum, point) => sum + point.revenue, 0);
  const averageRevenue = points.length > 0 ? Math.round(totalRevenue / points.length) : 0;
  const topPoint = points.reduce<DashboardRevenuePoint | null>((top, point) => {
    if (!top || point.revenue > top.revenue) return point;
    return top;
  }, null);
  const maxRevenue = Math.max(...points.map((point) => point.revenue), 1);

  return (
    <div className="rounded-2xl border border-[#AFD3E2]/70 bg-white shadow-xs">
      {/* Modern Compact Header */}
      <div className="border-b border-[#AFD3E2]/50 p-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.15em] text-[#19A7CE]">7-day Analytics</p>
            <h3 className="mt-1 text-xl font-bold text-slate-800">Completed Auction Revenue</h3>
          </div>
          <div className="flex items-center gap-3 rounded-xl bg-[#EAF7FB] px-4 py-2.5 ring-1 ring-[#AFD3E2]/50">
            <span className="text-xs font-semibold text-[#5A7184]">Total GMV:</span>
            <span className="text-lg font-bold text-[#146C94]">{formatCurrency(totalRevenue)}</span>
          </div>
        </div>
      </div>

      {/* Chart Layout Split */}
      <div className="grid gap-8 p-6 lg:grid-cols-[1fr_300px]">
        <div className="space-y-4">
          {points.map((point) => {
            const width = Math.max((point.revenue / maxRevenue) * 100, point.revenue > 0 ? 8 : 2);

            return (
              <div key={point.date} className="grid grid-cols-[60px_1fr_auto] items-center gap-4">
                <span className="text-xs font-medium text-[#5A7184]">{formatDate(point.date)}</span>
                <div className="h-3.5 overflow-hidden rounded-md bg-[#EAF7FB]/60 ring-1 ring-[#AFD3E2]/30">
                  <div
                    className="h-full rounded-md bg-linear-to-r from-[#146C94] to-[#19A7CE] transition-all duration-300 hover:brightness-105"
                    style={{ width: `${width}%` }}
                    title={formatCurrency(point.revenue)}
                  />
                </div>
                <span className="min-w-28 text-right text-xs font-bold text-[#146C94]">{formatCurrency(point.revenue)}</span>
              </div>
            );
          })}
        </div>

        {/* Highlight Stats Block */}
        <div className="flex flex-col justify-center gap-4 border-t border-[#AFD3E2]/40 pt-4 lg:border-t-0 lg:border-l lg:border-[#AFD3E2]/40 lg:pt-0 lg:pl-6">
          <div className="rounded-xl bg-[#F6FBFC] p-4 ring-1 ring-[#AFD3E2]/40">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-[#5A7184]">Average / Day</p>
            <p className="mt-1 text-lg font-bold text-[#146C94]">{formatCurrency(averageRevenue)}</p>
          </div>
          <div className="rounded-xl bg-[#F6FBFC] p-4 ring-1 ring-[#AFD3E2]/40">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-[#5A7184]">Best Performing Day</p>
            <p className="mt-1 text-lg font-bold text-[#146C94]">{topPoint ? formatCurrency(topPoint.revenue) : formatCurrency(0)}</p>
            {topPoint && <p className="mt-0.5 text-xs font-medium text-[#5A7184]/70">Achieved on {formatDate(topPoint.date)}</p>}
          </div>
        </div>
      </div>
    </div>
  );
}

function BreakdownList<T extends string>({ title, items }: { title: string; items: DashboardStatusCount<T>[] }) {
  const total = Math.max(items.reduce((sum, item) => sum + item.count, 0), 1);

  return (
    <div className="rounded-2xl border border-[#AFD3E2]/60 bg-white p-5 shadow-xs">
      <h3 className="text-sm font-bold uppercase tracking-wider text-[#146C94]">{title}</h3>
      <div className="mt-4 space-y-3.5">
        {items.map((item) => {
          const width = Math.max((item.count / total) * 100, item.count > 0 ? 6 : 2);

          return (
            <div key={item.status} className="group">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold capitalize text-slate-600 group-hover:text-slate-900 transition-colors">{statusLabel(item.status)}</span>
                <span className="font-bold text-[#146C94]">{numberFormatter.format(item.count)}</span>
              </div>
              <div className="mt-2 h-2 overflow-hidden rounded-md bg-[#EAF7FB]">
                <div className="h-full rounded-md bg-[#19A7CE] transition-all duration-500" style={{ width: `${width}%` }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function RecentAuctionsTable({ auctions }: { auctions: DashboardAuctionSummary[] }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-[#AFD3E2]/60 bg-white shadow-xs">
      <div className="border-b border-[#AFD3E2]/50 bg-[#F6FBFC]/60 px-6 py-5">
        <p className="text-[10px] font-bold uppercase tracking-widest text-[#5A7184]">Operations Feed</p>
        <h3 className="text-lg font-bold text-[#146C94]">Recent Auctions Activity</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[850px] text-left text-xs">
          <thead className="bg-[#F6FBFC]/30 text-[10px] font-bold uppercase tracking-wider text-[#5A7184] border-b border-[#AFD3E2]/30">
            <tr>
              <th className="px-6 py-3.5 font-semibold">Auction Name</th>
              <th className="px-6 py-3.5 font-semibold">Current Price</th>
              <th className="px-6 py-3.5 font-semibold">Status</th>
              <th className="px-6 py-3.5 font-semibold">Approval</th>
              <th className="px-6 py-3.5 font-semibold">Bids</th>
              <th className="px-6 py-3.5 font-semibold">Participants</th>
              <th className="px-6 py-3.5 font-semibold">Winning Price</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#AFD3E2]/30 text-slate-600">
            {auctions.length > 0 ? (
              auctions.map((auction) => (
                <tr key={auction.auctionId} className="transition-colors hover:bg-[#F6FBFC]/40">
                  <td className="px-6 py-4 font-semibold text-slate-800 max-w-[240px] truncate">{auction.productName}</td>
                  <td className="px-6 py-4 font-bold text-[#146C94]">{formatCurrency(auction.currentPrice)}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold capitalize ring-1 ${badgeClassName(auction.auctionStatus)}`}>
                      {statusLabel(auction.auctionStatus)}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold capitalize ring-1 ${badgeClassName(auction.approvalStatus)}`}>
                      {statusLabel(auction.approvalStatus)}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-medium text-slate-500">{numberFormatter.format(auction.totalBids)}</td>
                  <td className="px-6 py-4 font-medium text-slate-500">{numberFormatter.format(auction.totalParticipants)}</td>
                  <td className="px-6 py-4 font-bold text-slate-700">
                    {auction.winningPrice === null ? <span className="text-slate-300">—</span> : formatCurrency(auction.winningPrice)}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td className="px-6 py-10 text-center font-medium text-[#5A7184]" colSpan={7}>
                  No recent auctions recorded yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default async function AdminDashboardPage() {
  let stats;
  let errorMessage: string | null = null;

  try {
    stats = await fetchAdminDashboardStats();
  } catch (error) {
    errorMessage = error instanceof Error ? error.message : "Failed to load admin dashboard stats.";
  }

  if (errorMessage || !stats) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700">
        {errorMessage}
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-(screen-2xl) space-y-6 p-4 antialiased">
      {/* Top Banner section */}
      <section className="rounded-2xl border border-[#AFD3E2]/60 bg-white p-6 shadow-xs">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#19A7CE]">Admin Area</p>
            <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-slate-800 sm:text-3xl">Live Operations Dashboard</h1>
            <p className="mt-1.5 text-xs text-[#5A7184] max-w-2xl leading-relaxed">
              Real-time monitoring hub for user registrations, validation workflows, live auction feeds, and platform performance.
            </p>
          </div>
          <div className="shrink-0 rounded-2xl bg-[#F6FBFC] p-4 ring-1 ring-[#AFD3E2]/40 min-w-[200px]">
            <p className="text-[10px] font-bold uppercase tracking-wider text-[#5A7184]">Platform Cumulative GMV</p>
            <p className="mt-0.5 text-2xl font-black text-[#146C94] tracking-tight">{formatCurrency(stats.totalRevenue)}</p>
          </div>
        </div>
      </section>
      {/* Grid Indicators Layout */}
      <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Pending Users" value={numberFormatter.format(stats.pendingUsers)} note={`${stats.totalUsers} total users registered`} tone="text-orange-600" />
        <StatCard label="Pending Approval" value={numberFormatter.format(stats.pendingApprovalAuctions)} note={`${stats.approvedAuctions} currently active`} tone="text-orange-600" />
        <StatCard label="Live Auctions" value={numberFormatter.format(stats.liveAuctions)} note={`${stats.upcomingAuctions} upcoming entries`} tone="text-red-600" />
        <StatCard label="Total Bids Logged" value={numberFormatter.format(stats.totalBids)} note={`${stats.totalParticipants} distinct participants`} tone="text-[#146C94]" />
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <StatCard label="Active Users" value={numberFormatter.format(stats.activeUsers)} note="Approved accounts" tone="text-emerald-600" />
        <StatCard label="Locked Users" value={numberFormatter.format(stats.lockedUsers)} note="Restricted accounts" tone="text-slate-500" />
        <StatCard label="Completed Auctions" value={numberFormatter.format(stats.completedAuctions)} note={`${stats.failedAuctions} failed/unresolved`} tone="text-[#146C94]" />
      </div>

      {/* Breakdowns Sections */}
      <section className="grid grid-cols-1 gap-5 md:grid-cols-3">
        <BreakdownList title="Auction Distribution" items={stats.auctionStatusBreakdown} />
        <BreakdownList title="Approvals Metrics" items={stats.approvalStatusBreakdown} />
        <BreakdownList title="User Activity Status" items={stats.userStatusBreakdown} />
      </section>

      {/* Deep Analytics & Tables */}
      <RevenueChart points={stats.revenueLast7Days} />
      <RecentAuctionsTable auctions={stats.recentAuctions} />
    </div>
  );
}