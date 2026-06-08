import { fetchSellerDashboardStats } from "@/services/fetch_seller_dashboard_stats";
import type { DashboardAuctionSummary, DashboardRevenuePoint } from "@/types/dashboard_stats";

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

function statusClassName(status: string): string {
  if (status === "IN_PROGRESS") return "bg-red-50 text-red-700 ring-red-200";
  if (status === "NOT_STARTED") return "bg-emerald-50 text-emerald-700 ring-emerald-200";
  if (status === "COMPLETED") return "bg-[#EAF7FB] text-[#146C94] ring-[#AFD3E2]";
  if (status === "FAILED" || status === "REJECTED") return "bg-slate-100 text-slate-600 ring-slate-200";
  return "bg-orange-50 text-orange-700 ring-orange-200";
}

function StatCard({ label, value, note, tone = "text-[#146C94]" }: { label: string; value: string | number; note: string; tone?: string }) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-[#AFD3E2]/60 bg-white p-6 shadow-xs transition-all duration-300 hover:-translate-y-0.5 hover:shadow-md hover:shadow-[#19A7CE]/5">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[#5A7184]">{label}</p>
      <p className={`mt-2 text-3xl font-extrabold tracking-tight ${tone}`}>{value}</p>
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
      {/* Modern Compact Header - Y hệt Admin style */}
      <div className="border-b border-[#AFD3E2]/50 p-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.15em] text-[#19A7CE]">Revenue trend</p>
            <h3 className="mt-1 text-xl font-bold text-slate-800">Revenue Growth (Last 7 Days)</h3>
          </div>
          <div className="flex items-center gap-3 rounded-xl bg-[#EAF7FB] px-4 py-2.5 ring-1 ring-[#AFD3E2]/50">
            <span className="text-xs font-semibold text-[#5A7184]">Total Interval GMV:</span>
            <span className="text-lg font-bold text-[#146C94]">{formatCurrency(totalRevenue)}</span>
          </div>
        </div>
      </div>

      {/* Chart Layout Split - Chuyển sang thanh ngang và thêm sidebar thống kê phụ */}
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

        {/* Highlight Stats Block Sidebar */}
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

function RecentAuctionsTable({ auctions }: { auctions: DashboardAuctionSummary[] }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-[#AFD3E2]/60 bg-white shadow-xs">
      <div className="border-b border-[#AFD3E2]/50 bg-[#F6FBFC]/60 px-6 py-5">
        <p className="text-[10px] font-bold uppercase tracking-widest text-[#5A7184]">Auction activity</p>
        <h3 className="text-lg font-bold text-[#146C94]">Recent Auctions</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[760px] text-left text-xs">
          <thead className="bg-[#F6FBFC]/30 text-[10px] font-bold uppercase tracking-wider text-[#5A7184] border-b border-[#AFD3E2]/30">
            <tr>
              <th className="px-6 py-3.5 font-semibold">Auction Name</th>
              <th className="px-6 py-3.5 font-semibold">Current Price</th>
              <th className="px-6 py-3.5 font-semibold">Status</th>
              <th className="px-6 py-3.5 font-semibold">Approval</th>
              <th className="px-6 py-3.5 font-semibold">Bids</th>
              <th className="px-6 py-3.5 font-semibold">Participants</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#AFD3E2]/30 text-slate-600">
            {auctions.length > 0 ? (
              auctions.map((auction) => (
                <tr key={auction.auctionId} className="transition-colors hover:bg-[#F6FBFC]/40">
                  <td className="px-6 py-4 font-semibold text-slate-800 max-w-[240px] truncate">{auction.productName}</td>
                  <td className="px-6 py-4 font-bold text-[#146C94]">{formatCurrency(auction.currentPrice)}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold capitalize ring-1 ${statusClassName(auction.auctionStatus)}`}>
                      {statusLabel(auction.auctionStatus)}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold capitalize ring-1 ${statusClassName(auction.approvalStatus)}`}>
                      {statusLabel(auction.approvalStatus)}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-medium text-slate-500">{numberFormatter.format(auction.totalBids)}</td>
                  <td className="px-6 py-4 font-medium text-slate-500">{numberFormatter.format(auction.totalParticipants)}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td className="px-6 py-10 text-center font-medium text-[#5A7184]" colSpan={6}>
                  No recent auctions records yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default async function SellerDashboardPage() {
  let stats;
  let errorMessage: string | null = null;

  try {
    stats = await fetchSellerDashboardStats();
  } catch (error) {
    errorMessage = error instanceof Error ? error.message : "Failed to load seller dashboard stats.";
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
      {/* Top Main Banner Panel - Đã đồng bộ phẳng nền theo Admin */}
      <section className="rounded-2xl border border-[#AFD3E2]/60 bg-white p-6 shadow-xs">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#19A7CE]">Seller dashboard</p>
            <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-slate-800 sm:text-3xl">Your auction performance</h1>
            <p className="mt-1.5 text-xs text-[#5A7184] max-w-2xl leading-relaxed">
              Track moderation status, live workload, bidding activity, and completed auction revenue.
            </p>
          </div>
          <div className="shrink-0 rounded-2xl bg-[#F6FBFC] p-4 ring-1 ring-[#AFD3E2]/40 min-w-[200px]">
            <p className="text-[10px] font-bold uppercase tracking-wider text-[#5A7184]">Total revenue</p>
            <p className="mt-0.5 text-2xl font-black text-[#146C94] tracking-tight">{formatCurrency(stats.totalRevenue)}</p>
          </div>
        </div>
      </section>

      {/* Primary Metrics Indicators Grid */}
      <section className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total Auctions" value={numberFormatter.format(stats.totalAuctions)} note={`${stats.approvedAuctions} approved items`} />
        <StatCard label="Pending Approval" value={numberFormatter.format(stats.pendingApprovalAuctions)} note="Waiting for admin moderation" tone="text-orange-600" />
        <StatCard label="Live Auctions" value={numberFormatter.format(stats.liveAuctions)} note={`${stats.upcomingAuctions} upcoming scheduled`} tone="text-red-600" />
        <StatCard label="Total Bids" value={numberFormatter.format(stats.totalBids)} note={`${stats.totalParticipants} participant records`} />
      </section>

      {/* Secondary Status Analysis Grid */}
      <section className="grid grid-cols-1 gap-5 md:grid-cols-3">
        <StatCard label="Completed" value={numberFormatter.format(stats.completedAuctions)} note="Auctions with final result" tone="text-emerald-600" />
        <StatCard label="Rejected" value={numberFormatter.format(stats.rejectedAuctions)} note="Need review before resubmission" tone="text-slate-500" />
        <StatCard label="Failed" value={numberFormatter.format(stats.failedAuctions)} note="Closed without completion" tone="text-red-600" />
      </section>

      {/* Graphs Analytics & Logs Grid */}
      <RevenueChart points={stats.revenueLast7Days} />
      <RecentAuctionsTable auctions={stats.recentAuctions} />
    </div>
  );
}