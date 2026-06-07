import { getErrorStatus } from "@/lib/app_error";
import { fetchPublicAuction } from "@/services/fetch_public_auction";
import { notFound } from "next/navigation";
import AdminLiveAuctionView from "@/components/admin/live/admin_live_auction_view";
import Link from "next/link";

export default async function AdminLivePage({
  params,
}: {
  params: Promise<{ auction_id: string }>;
}) {
  const { auction_id: auctionId } = await params;
  let auction;
  try {
    auction = await fetchPublicAuction(auctionId);
  } catch (error) {
    if (getErrorStatus(error) === 404) notFound();
    throw error;
  }

  const backendServerUrl =
    process.env.PUBLIC_BACKEND_SERVER_URL ||
    process.env.BACKEND_SERVER_URL ||
    "";

  return (
    <div className="space-y-4">
      <nav className="flex flex-col gap-3 rounded-2xl border border-[#AFD3E2] bg-white px-5 py-4 shadow-sm shadow-[#AFD3E2]/20 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex flex-wrap items-center gap-2 text-sm text-[#5A7184]">
          <Link
            href="/admin-dashboard/auctions"
            className="rounded-full bg-[#EAF7FB] px-3 py-1.5 font-semibold text-[#146C94] transition hover:bg-[#D7EFF7]"
          >
            Auctions
          </Link>
          <span className="text-[#AFD3E2]">/</span>
          <span className="font-semibold text-[#146C94]">{auction.product_name}</span>
          <span className="text-[#AFD3E2]">/</span>
          <span className="font-semibold text-[#19A7CE]">Live Monitor</span>
        </div>
        <div className="text-xs font-medium uppercase tracking-[0.22em] text-[#5A7184]">
          Real-time admin monitor
        </div>
      </nav>
      <AdminLiveAuctionView
        auction={auction}
        backendServerUrl={backendServerUrl}
      />
    </div>
  );
}
