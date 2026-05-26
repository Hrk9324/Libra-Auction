'use client';

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import AuctionDetailModal from "@/components/admin/auction_detail_modal";
import { approveAuction } from "@/services/approve_auction";
import { fetchApprovedAuctions } from "@/services/fetch_approved_auctions";
import { fetchPendingAuctions } from "@/services/fetch_pending_auctions";
import { fetchRejectedAuctions } from "@/services/fetch_rejected_auctions";
import { rejectAuction } from "@/services/reject_auction";
import { AdminAuction } from "@/types/admin/admin_auction";
import { DateFormat } from "@/utils/date_format";

type AuctionRow = AdminAuction & {
  id: string;
  image: string;
  name: string;
  category: string;
  startingPrice: number;
  startTime: string;
  endTime: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  images: string[];
  productName: string;
};

type AuctionFilter = "ALL" | "PENDING" | "APPROVED" | "REJECTED";

function resolveAuctionStatus(status: string): "PENDING" | "APPROVED" | "REJECTED" {
  if (status === "DA_DUYET") return "APPROVED";
  if (status === "BI_TU_CHOI") return "REJECTED";
  return "PENDING";
}

function formatPrice(price: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(price);
}

function mapAuction(auction: AdminAuction): AuctionRow {
  const startDate = new Date(auction.start_time);
  const endDate = new Date(startDate.getTime() + auction.duration * 60 * 1000);

  return {
    ...auction,
    id: auction.auction_id,
    image: auction.images?.[0] || "/default-avatar.png",
    name: auction.product_name,
    category: auction.category_name || "-",
    startingPrice: auction.starting_price,
    startTime: DateFormat(startDate),
    endTime: DateFormat(endDate),
    status: resolveAuctionStatus(auction.approval_status),
    images: auction.images || [],
    productName: auction.product_name,
  };
}

function getStatusBadge(status: string) {
  const statusStyles: Record<string, string> = {
    PENDING: "bg-amber-100 text-amber-800 border border-amber-300",
    APPROVED: "bg-green-100 text-green-800 border border-green-300",
    REJECTED: "bg-red-100 text-red-800 border border-red-300",
  };

  const labels: Record<string, string> = {
    PENDING: "Pending",
    APPROVED: "Approved",
    REJECTED: "Rejected",
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusStyles[status] || "bg-gray-100 text-gray-700 border border-gray-200"}`}>
      {labels[status] || status}
    </span>
  );
}

export default function AuctionsApprovalPage() {
  const [auctions, setAuctions] = useState<AuctionRow[]>([]);
  const [selectedAuction, setSelectedAuction] = useState<{ isOpen: boolean; data: AuctionRow | null }>({
    isOpen: false,
    data: null,
  });
  const [statusFilter, setStatusFilter] = useState<AuctionFilter>("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  useEffect(() => {
    const loadAuctions = async () => {
      try {
        setLoading(true);
        setError(null);

        const [pending, approved, rejected] = await Promise.all([
          fetchPendingAuctions(0, 1000),
          fetchApprovedAuctions(0, 1000),
          fetchRejectedAuctions(0, 1000),
        ]);

        const combined = [...pending.content, ...approved.content, ...rejected.content]
          .map(mapAuction)
          .sort((left, right) => new Date(right.start_time).getTime() - new Date(left.start_time).getTime());

        setAuctions(combined);
      } catch (loadError) {
        setError(loadError instanceof Error ? loadError.message : "Failed to load auctions");
      } finally {
        setLoading(false);
      }
    };

    loadAuctions();
  }, []);

  const pendingCount = useMemo(() => auctions.filter((auction) => auction.status === "PENDING").length, [auctions]);
  const approvedCount = useMemo(() => auctions.filter((auction) => auction.status === "APPROVED").length, [auctions]);
  const rejectedCount = useMemo(() => auctions.filter((auction) => auction.status === "REJECTED").length, [auctions]);

  const filteredAuctions = statusFilter === "ALL"
    ? auctions
    : auctions.filter((auction) => auction.status === statusFilter);

  const handleApprove = async (auction: AuctionRow) => {
    try {
      setActionLoadingId(auction.id);
      const updatedAuction = mapAuction(await approveAuction(auction.id));
      setAuctions((current) => current.map((item) => (item.id === updatedAuction.id ? updatedAuction : item)));
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : "Failed to approve auction");
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleReject = async (auction: AuctionRow) => {
    try {
      setActionLoadingId(auction.id);
      const updatedAuction = mapAuction(await rejectAuction(auction.id));
      setAuctions((current) => current.map((item) => (item.id === updatedAuction.id ? updatedAuction : item)));
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : "Failed to reject auction");
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Pending</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{pendingCount}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Approved</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{approvedCount}</p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-[#AFD3E2] shadow-sm shadow-[#AFD3E2]/20">
          <p className="text-xs font-semibold text-[#5A7184] uppercase">Rejected</p>
          <p className="text-2xl font-bold text-[#146C94] mt-1">{rejectedCount}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
        <h3 className="text-lg font-bold text-[#146C94] mb-4">Filter by Status</h3>
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
            onClick={() => setStatusFilter("PENDING")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "PENDING"
                ? "bg-amber-500 text-white"
                : "bg-amber-50 text-amber-800 hover:bg-amber-100"
            }`}
          >
            Pending ({pendingCount})
          </button>
          <button
            onClick={() => setStatusFilter("APPROVED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "APPROVED"
                ? "bg-green-600 text-white"
                : "bg-green-50 text-green-800 hover:bg-green-100"
            }`}
          >
            Approved ({approvedCount})
          </button>
          <button
            onClick={() => setStatusFilter("REJECTED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "REJECTED"
                ? "bg-red-600 text-white"
                : "bg-red-50 text-red-800 hover:bg-red-100"
            }`}
          >
            Rejected ({rejectedCount})
          </button>
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
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Image</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Product Name</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Product Description</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Category</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Starting Price</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Start Time</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Status</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td className="px-6 py-8 text-center text-[#5A7184]" colSpan={8}>
                    Loading...
                  </td>
                </tr>
              ) : filteredAuctions.length === 0 ? (
                <tr>
                  <td className="px-6 py-8 text-center text-[#5A7184]" colSpan={8}>
                    No auctions available
                  </td>
                </tr>
              ) : (
                filteredAuctions.map((row) => (
                  <tr key={row.id} className="border-b border-[#EAF3F6] hover:bg-[#F8FCFD]">
                    <td className="px-6 py-4">
                      <Image
                        src={row.image}
                        alt={row.name}
                        width={60}
                        height={60}
                        className="w-16 h-16 rounded-lg object-cover"
                        onError={(e) => {
                          e.currentTarget.src = "/default-avatar.png";
                        }}
                      />
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-[#5A7184]">{row.name}</td>
                    <td className="px-6 py-4 text-sm text-[#5A7184] max-w-md">
                      <span className="block truncate" title={row.description}>
                        {row.description || "-"}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.category}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-[#19A7CE]">{formatPrice(row.startingPrice)}</td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.startTime}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.status)}</td>
                    <td className="px-6 py-4 text-sm">
                      <div className="flex gap-2 flex-wrap">
                        <button
                          onClick={() => setSelectedAuction({ isOpen: true, data: row })}
                          className="px-3 py-1 bg-[#EAF7FB] text-[#146C94] rounded hover:bg-[#D7EFF7] text-xs font-semibold"
                        >
                          Details
                        </button>
                        <button
                          onClick={() => handleApprove(row)}
                          disabled={actionLoadingId === row.id || row.status !== "PENDING"}
                          className="px-3 py-1 bg-green-50 text-green-700 rounded hover:bg-green-100 text-xs font-semibold disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleReject(row)}
                          disabled={actionLoadingId === row.id || row.status !== "PENDING"}
                          className="px-3 py-1 bg-red-50 text-red-700 rounded hover:bg-red-100 text-xs font-semibold disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedAuction.data && (
        <AuctionDetailModal
          isOpen={selectedAuction.isOpen}
          onClose={() => setSelectedAuction({ isOpen: false, data: null })}
          auctionData={{
            productName: selectedAuction.data.productName,
            description: selectedAuction.data.description,
            startTime: selectedAuction.data.startTime,
            endTime: selectedAuction.data.endTime,
            images: selectedAuction.data.images,
            startingPrice: selectedAuction.data.startingPrice,
            currentPrice: selectedAuction.data.current_price,
            category: selectedAuction.data.category,
            status: selectedAuction.data.status,
            totalBids: selectedAuction.data.total_bids,
            totalParticipants: selectedAuction.data.total_participants,
          }}
        />
      )}
    </div>
  );
}