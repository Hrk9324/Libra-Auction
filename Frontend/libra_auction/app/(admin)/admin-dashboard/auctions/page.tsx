'use client';

import { useEffect, useMemo, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import AuctionDetailModal from "@/components/admin/auction_detail_modal";
import { approveAuction } from "@/services/approve_auction";
import { completeAuction } from "@/services/complete_auction";
import { failAuction } from "@/services/fail_auction";
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
  auctionStatus: string;
  images: string[];
  productName: string;
  failure_reason?: string;
  completed_at?: string;
};

type AuctionFilter = "ALL" | "PENDING" | "APPROVED" | "REJECTED";

function resolveAuctionStatus(status: string): "PENDING" | "APPROVED" | "REJECTED" {
  if (status === "APPROVED") return "APPROVED";
  if (status === "REJECTED") return "REJECTED";
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
  const endDate = new Date(startDate.getTime() + auction.duration * 1000);

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
    auctionStatus: auction.auction_status || "NOT_STARTED",
    images: auction.images || [],
    productName: auction.product_name,
    failure_reason: auction.failure_reason,
    completed_at: auction.completed_at,
  };
}

function getStatusBadge(status: string, auctionStatus?: string) {
  if (auctionStatus === "COMPLETED") {
    return <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-300">Completed</span>;
  }
  if (auctionStatus === "FAILED") {
    return <span className="px-3 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-800 border border-rose-300">Failed</span>;
  }

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

function getAuctionStatusBadge(auctionStatus: string) {
  const styles: Record<string, string> = {
    NOT_STARTED: "bg-gray-100 text-gray-600",
    IN_PROGRESS: "bg-blue-100 text-blue-700",
    PAUSED: "bg-yellow-100 text-yellow-700",
    ENDED: "bg-purple-100 text-purple-700",
    COMPLETED: "bg-green-100 text-green-700",
    FAILED: "bg-red-100 text-red-700",
    CANCELLED: "bg-gray-100 text-gray-500",
  };

  return (
    <span className={`px-2 py-0.5 rounded text-xs font-medium ${styles[auctionStatus] || "bg-gray-100 text-gray-600"}`}>
      {auctionStatus}
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
  const [failDialog, setFailDialog] = useState<{ isOpen: boolean; auctionId: string | null; reason: string }>({
    isOpen: false,
    auctionId: null,
    reason: "",
  });

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

  const handleComplete = async (auction: AuctionRow) => {
    if (!confirm(`Xác nhận hoàn thành phiên đấu giá "${auction.name}"? Sản phẩm sẽ được đánh dấu là đã bán.`)) return;
    try {
      setActionLoadingId(auction.id);
      await completeAuction(auction.id);
      setAuctions((current) => current.map((item) =>
        item.id === auction.id ? { ...item, auctionStatus: "COMPLETED" } : item
      ));
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : "Failed to complete auction");
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleFail = async () => {
    if (!failDialog.auctionId || !failDialog.reason.trim()) return;
    try {
      setActionLoadingId(failDialog.auctionId);
      await failAuction(failDialog.auctionId, failDialog.reason.trim());
      setAuctions((current) => current.map((item) =>
        item.id === failDialog.auctionId
          ? { ...item, auctionStatus: "FAILED", failure_reason: failDialog.reason.trim() }
          : item
      ));
      setFailDialog({ isOpen: false, auctionId: null, reason: "" });
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : "Failed to mark auction as failed");
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
          {(["ALL", "PENDING", "APPROVED", "REJECTED"] as AuctionFilter[]).map((filter) => (
            <button
              key={filter}
              onClick={() => setStatusFilter(filter)}
              className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
                statusFilter === filter
                  ? filter === "ALL"
                    ? "bg-[#19A7CE] text-white shadow-sm shadow-[#19A7CE]/30"
                    : filter === "PENDING"
                    ? "bg-amber-500 text-white"
                    : filter === "APPROVED"
                    ? "bg-green-600 text-white"
                    : "bg-red-600 text-white"
                  : filter === "ALL"
                  ? "bg-[#F6FBFC] text-[#5A7184] hover:bg-[#EAF7FB]"
                  : filter === "PENDING"
                  ? "bg-amber-50 text-amber-800 hover:bg-amber-100"
                  : filter === "APPROVED"
                  ? "bg-green-50 text-green-800 hover:bg-green-100"
                  : "bg-red-50 text-red-800 hover:bg-red-100"
              }`}
            >
              {filter === "ALL" ? "All" : filter.charAt(0) + filter.slice(1).toLowerCase()} ({filter === "ALL" ? auctions.length : filter === "PENDING" ? pendingCount : filter === "APPROVED" ? approvedCount : rejectedCount})
            </button>
          ))}
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
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Product</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Category</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Starting Price</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Start Time</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Approval</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-[#146C94]">Auction</th>
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
                    <td className="px-6 py-4">
                      <p className="text-sm font-medium text-[#5A7184]">{row.name}</p>
                      <p className="text-xs text-gray-400 truncate max-w-xs">{row.description || "-"}</p>
                    </td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.category}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-[#19A7CE]">{formatPrice(row.startingPrice)}</td>
                    <td className="px-6 py-4 text-sm text-[#5A7184]">{row.startTime}</td>
                    <td className="px-6 py-4 text-sm">{getStatusBadge(row.status, row.auctionStatus)}</td>
                    <td className="px-6 py-4 text-sm">{getAuctionStatusBadge(row.auctionStatus)}</td>
                    <td className="px-6 py-4 text-sm">
                      <div className="flex gap-2 flex-wrap">
                        <button
                          onClick={() => setSelectedAuction({ isOpen: true, data: row })}
                          className="px-3 py-1 bg-[#EAF7FB] text-[#146C94] rounded hover:bg-[#D7EFF7] text-xs font-semibold"
                        >
                          Details
                        </button>

                        {row.status === "APPROVED" &&
                          (row.auctionStatus === "IN_PROGRESS" || row.auctionStatus === "PAUSED") && (
                            <Link
                              href={`/admin-dashboard/auctions/${row.id}/live`}
                              className="px-3 py-1 bg-green-50 text-green-700 rounded hover:bg-green-100 text-xs font-semibold inline-flex items-center gap-1"
                            >
                              <span className="relative flex h-1.5 w-1.5">
                                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75" />
                                <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-500" />
                              </span>
                              Live
                            </Link>
                          )}

                        {/* Approve/Reject for PENDING */}
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
                        {row.status === "APPROVED" && row.auctionStatus === "ENDED" && (
                          <>
                            <button
                              onClick={() => handleComplete(row)}
                              disabled={actionLoadingId === row.id}
                              className="px-3 py-1 bg-emerald-50 text-emerald-700 rounded hover:bg-emerald-100 text-xs font-semibold disabled:cursor-not-allowed disabled:opacity-50"
                            >
                              Complete
                            </button>
                            <button
                              onClick={() => setFailDialog({ isOpen: true, auctionId: row.id, reason: "" })}
                              disabled={actionLoadingId === row.id}
                              className="px-3 py-1 bg-rose-50 text-rose-700 rounded hover:bg-rose-100 text-xs font-semibold disabled:cursor-not-allowed disabled:opacity-50"
                            >
                              Fail
                            </button>
                          </>
                        )}

                        {/* Show result for COMPLETED/FAILED */}
                        {row.auctionStatus === "COMPLETED" && (
                          <span className="px-3 py-1 bg-green-50 text-green-700 rounded text-xs font-semibold">
                            Completed
                          </span>
                        )}
                        {row.auctionStatus === "FAILED" && (
                          <span
                            className="px-3 py-1 bg-red-50 text-red-700 rounded text-xs font-semibold cursor-help"
                            title={row.failure_reason || "No reason provided"}
                          >
                            Failed
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Failure Reason Dialog */}
      {failDialog.isOpen && (
        <>
          <div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50"
            onClick={() => setFailDialog({ isOpen: false, auctionId: null, reason: "" })}
          />
          <div className="fixed inset-0 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md" onClick={(e) => e.stopPropagation()}>
              <div className="px-6 py-5 border-b border-gray-200">
                <h3 className="text-xl font-bold text-red-600">Đánh dấu phiên đấu giá thất bại</h3>
              </div>
              <div className="px-6 py-5">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Lý do thất bại <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={failDialog.reason}
                  onChange={(e) => setFailDialog((prev) => ({ ...prev, reason: e.target.value }))}
                  placeholder="Nhập lý do phiên đấu giá thất bại..."
                  rows={4}
                  className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-red-300 focus:border-red-400 text-sm resize-none"
                />
                <p className="text-xs text-gray-400 mt-2">
                  Sản phẩm sẽ được chuyển về trạng thái &quot;Sẵn sàng&quot; và có thể tạo phiên đấu giá mới.
                </p>
              </div>
              <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-200 bg-gray-50 rounded-b-2xl">
                <button
                  onClick={() => setFailDialog({ isOpen: false, auctionId: null, reason: "" })}
                  className="px-5 py-2.5 border border-gray-300 rounded-lg text-gray-700 font-medium text-sm hover:bg-gray-100 transition"
                >
                  Hủy
                </button>
                <button
                  onClick={handleFail}
                  disabled={!failDialog.reason.trim() || actionLoadingId === failDialog.auctionId}
                  className="px-5 py-2.5 bg-red-600 text-white rounded-lg font-medium text-sm hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {actionLoadingId === failDialog.auctionId ? "Đang xử lý..." : "Xác nhận thất bại"}
                </button>
              </div>
            </div>
          </div>
        </>
      )}

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
            auctionStatus: selectedAuction.data.auctionStatus,
            totalBids: selectedAuction.data.total_bids,
            totalParticipants: selectedAuction.data.total_participants,
            failureReason: selectedAuction.data.failure_reason,
            completedAt: selectedAuction.data.completed_at,
          }}
        />
      )}
    </div>
  );
}
