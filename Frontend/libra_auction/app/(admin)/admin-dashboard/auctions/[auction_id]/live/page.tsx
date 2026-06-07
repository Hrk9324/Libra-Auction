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
    <>
      {/* Breadcrumb */}
      <nav className="mb-4 text-sm text-[#5A7184]">
        <Link href="/admin-dashboard/auctions" className="hover:text-[#146C94]">
          Auctions
        </Link>
        <span className="mx-2">/</span>
        <span className="text-[#146C94]">{auction.product_name}</span>
        <span className="mx-2">/</span>
        <span className="text-[#19A7CE] font-semibold">Live Monitor</span>
      </nav>
      <AdminLiveAuctionView
        auction={auction}
        backendServerUrl={backendServerUrl}
      />
    </>
  );
}
