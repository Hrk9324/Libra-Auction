"use client";

import { useEffect, useState, useCallback } from "react";
import Image from "next/image";
import { Auction } from "@/types/auction/auction";
import { CurrencyFormat } from "@/utils/currency_format";
import { auctionSocket } from "@/services/auction_socket";
import AdminStatsPanel from "./admin_stats_panel";
import AdminControls from "./admin_controls";
import BidHistory, { BidEntry } from "@/components/shared/bid_history";
import AuctionTimer from "@/components/shared/auction_timer";

type StatusUpdate = {
  type?: string;
  auctionId?: string;
  status?: string;
  message?: string;
  newEndTime?: string;
  timestamp?: string;
};

type BidUpdate = {
  auctionId?: string;
  bidAmount?: number;
  bidderId?: string;
  bidderName?: string;
  bidTime?: string;
  status?: string;
  currentPrice?: number;
  current_price?: number;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

export default function AdminLiveAuctionView({
  auction,
  backendServerUrl,
}: {
  auction: Auction;
  backendServerUrl: string;
}) {
  const [currentPrice, setCurrentPrice] = useState(
    auction.current_price || auction.starting_price || 0
  );
  const [endTimeMs] = useState(() => {
    const end =
      new Date(auction.start_time).getTime() + auction.duration * 1000;
    return end;
  });
  const [auctionStatus, setAuctionStatus] = useState<string>(
    auction.auction_status || "IN_PROGRESS"
  );
  const [bids, setBids] = useState<BidEntry[]>([]);
  const [totalBids, setTotalBids] = useState(auction.total_bids || 0);
  const [viewerCount, setViewerCount] = useState(0);
  const [participantCount, setParticipantCount] = useState(
    auction.total_participants || 0
  );
  const [highestBidder, setHighestBidder] = useState("--");
  const [notifications, setNotifications] = useState<string[]>([]);
  const [isActionLoading, setIsActionLoading] = useState(false);

  const addNotification = useCallback((msg: string) => {
    setNotifications((prev) => [
      `${new Date().toLocaleTimeString("vi-VN")} - ${msg}`,
      ...prev.slice(0, 49),
    ]);
  }, []);

  useEffect(() => {
    const bidsTopic = `/topic/auction/${auction.auction_id}/bids`;
    const statusTopic = `/topic/auction/${auction.auction_id}/status`;
    const genericTopic = `/topic/auction/${auction.auction_id}`;
    const adminTopic = `/topic/auction/${auction.auction_id}/admin`;

    auctionSocket.connect(backendServerUrl);

    auctionSocket.subscribe(bidsTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const bid = body as unknown as BidUpdate;
      if (typeof bid.bidAmount === "number") {
        setCurrentPrice(bid.bidAmount);
        setBids((prev) => [
          {
            bidderName: bid.bidderName || "Unknown",
            amount: bid.bidAmount!,
            time: bid.bidTime
              ? new Date(bid.bidTime).toLocaleTimeString("vi-VN")
              : new Date().toLocaleTimeString("vi-VN"),
            status: (bid.status as BidEntry["status"]) || "SUCCESS",
          },
          ...prev,
        ]);
        setTotalBids((prev) => prev + 1);
        setHighestBidder(bid.bidderName || "--");
      }
      // Also check for currentPrice in generic format
      const price = bid.currentPrice ?? bid.current_price;
      if (typeof price === "number" && price > 0) {
        setCurrentPrice(price);
      }
    });

    auctionSocket.subscribe(statusTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const update = body as unknown as StatusUpdate;
      if (update.status) {
        setAuctionStatus(update.status);
      }
      if (update.message) {
        addNotification(update.message);
      }
      if (update.type) {
        switch (update.type) {
          case "AUCTION_PAUSED":
            setAuctionStatus("PAUSED");
            addNotification("Phiên đấu giá đã bị tạm dừng");
            break;
          case "AUCTION_RESUMED":
            setAuctionStatus("IN_PROGRESS");
            addNotification("Phiên đấu giá đã tiếp tục");
            break;
          case "AUCTION_ENDED":
            setAuctionStatus("ENDED");
            addNotification("Phiên đấu giá đã kết thúc");
            break;
          case "AUCTION_CANCELLED":
            setAuctionStatus("CANCELLED");
            addNotification("Phiên đấu giá đã bị hủy");
            break;
          case "AUCTION_EXTENDED":
            addNotification("Phiên đấu giá đã được gia hạn");
            break;
        }
      }
    });

    auctionSocket.subscribe(genericTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const bid = body as unknown as BidUpdate;
      const price = bid.currentPrice ?? bid.current_price;
      if (typeof price === "number") {
        setCurrentPrice(price);
      }
    });

    auctionSocket.subscribe(adminTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const msg = body as { type?: string; message?: string };
      if (msg.message) {
        addNotification(`[Admin] ${msg.message}`);
      }
    });

    return () => {
      auctionSocket.unsubscribe(bidsTopic);
      auctionSocket.unsubscribe(statusTopic);
      auctionSocket.unsubscribe(genericTopic);
      auctionSocket.unsubscribe(adminTopic);
    };
  }, [auction.auction_id, backendServerUrl, addNotification]);

  // Simulate viewer count (in production, this comes from WebSocket)
  useEffect(() => {
    const interval = setInterval(() => {
      setViewerCount((prev) => {
        const delta = Math.floor(Math.random() * 3) - 1;
        return Math.max(0, prev + delta);
      });
    }, 5000);
    // Set initial viewer count
    setViewerCount(
      auction.total_participants
        ? Math.max(1, Math.floor(auction.total_participants * 1.5))
        : 3
    );
    return () => clearInterval(interval);
  }, [auction.total_participants]);

  const sendAdminCommand = async (
    command: "pause" | "resume" | "end" | "cancel",
    reason?: string
  ) => {
    setIsActionLoading(true);
    try {
      auctionSocket.send(
        `/app/admin/auction/${auction.auction_id}/${command}`,
        {
          auctionId: auction.auction_id,
          command,
          ...(reason ? { reason } : {}),
        }
      );
    } catch (err) {
      console.error(`Admin command ${command} failed:`, err);
      addNotification(`Lỗi khi gửi lệnh ${command}`);
    } finally {
      setTimeout(() => setIsActionLoading(false), 1000);
    }
  };

  const handleSendNotification = (message: string) => {
    auctionSocket.send(
      `/app/admin/auction/${auction.auction_id}/notify`,
      {
        auctionId: auction.auction_id,
        message,
      }
    );
    addNotification(`Đã gửi thông báo: ${message}`);
  };

  const isEnded =
    auctionStatus === "ENDED" || auctionStatus === "CANCELLED";
  const isPaused = auctionStatus === "PAUSED";

  const statusLabel =
    auctionStatus === "IN_PROGRESS"
      ? "LIVE"
      : auctionStatus === "PAUSED"
      ? "PAUSED"
      : auctionStatus === "ENDED"
      ? "ENDED"
      : auctionStatus === "CANCELLED"
      ? "CANCELLED"
      : auctionStatus;

  const statusClassName =
    auctionStatus === "IN_PROGRESS"
      ? "bg-emerald-50 text-emerald-700 border-emerald-200"
      : auctionStatus === "PAUSED"
      ? "bg-amber-50 text-amber-700 border-amber-200"
      : auctionStatus === "ENDED"
      ? "bg-slate-100 text-slate-700 border-slate-200"
      : "bg-rose-50 text-rose-700 border-rose-200";

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-[#AFD3E2] bg-white p-6 shadow-sm shadow-[#AFD3E2]/20">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold text-[#146C94]">
              {auction.product_name}
            </h1>
            <p className="text-sm text-[#5A7184]">
              Auction ID {auction.auction_id} • {auction.category_name} • SL {auction.quantity}
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <span className={`inline-flex items-center rounded-full border px-3 py-1.5 text-sm font-semibold ${statusClassName}`}>
              {statusLabel}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Product Info */}
          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start">
              <div className="relative aspect-4/3 w-full overflow-hidden rounded-2xl bg-[#F6FBFC] xl:w-64 xl:flex-none">
                <Image
                  src={auction.images?.[0] || "/placeholder-image.jpg"}
                  alt={auction.product_name}
                  fill
                  className="object-cover"
                />
              </div>
              <div className="flex-1 space-y-4">
                <h2 className="text-xl font-bold text-[#146C94]">
                  {auction.product_name}
                </h2>
                <p className="text-sm text-[#5A7184]">
                  {auction.category_name} • SL: {auction.quantity}
                </p>
                <p className="text-sm leading-relaxed text-[#5A7184] line-clamp-3">
                  {auction.description}
                </p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5A7184]">Giá khởi điểm</p>
                    <p className="mt-2 text-lg font-bold text-[#19A7CE]">
                      {CurrencyFormat(auction.starting_price)}
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5A7184]">Bước giá tối thiểu</p>
                    <p className="mt-2 text-lg font-bold text-[#19A7CE]">
                      {CurrencyFormat(auction.min_bid_increment)}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <div className="mb-4 flex items-end justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#5A7184]">
                  Thời gian còn lại
                </p>
                <p className="mt-1 text-sm text-[#5A7184]">
                  Cập nhật theo trạng thái phiên đấu giá
                </p>
              </div>
            </div>
            <AuctionTimer
              endTimeMs={endTimeMs}
              isPaused={isPaused}
              size="lg"
            />
          </div>

          {/* Bid History */}
          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Lịch sử trả giá ({totalBids} bids)
            </h3>
            <BidHistory bids={bids} maxHeight="400px" />
          </div>

          {/* Notifications Log */}
          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Nhật ký thông báo
            </h3>
            <div className="max-h-48 overflow-auto space-y-1">
              {notifications.length === 0 ? (
                <p className="text-sm text-[#5A7184]">Chưa có thông báo</p>
              ) : (
                notifications.map((note, idx) => (
                  <p
                    key={idx}
                    className="text-xs text-[#5A7184] border-b border-gray-100 py-1"
                  >
                    {note}
                  </p>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Right Column - Stats & Controls */}
        <div className="space-y-6 lg:sticky lg:top-6 self-start">
          {/* Stats Panel */}
          <AdminStatsPanel
            currentPrice={currentPrice}
            endTimeMs={endTimeMs}
            viewerCount={viewerCount}
            participantCount={participantCount}
            totalBids={totalBids}
            highestBidder={highestBidder}
            auctionStatus={auctionStatus}
          />

          {/* Admin Controls */}
          <AdminControls
            auctionId={auction.auction_id}
            currentStatus={auctionStatus}
            onPause={() => sendAdminCommand("pause")}
            onResume={() => sendAdminCommand("resume")}
            onEnd={() => sendAdminCommand("end")}
            onCancel={(reason) => sendAdminCommand("cancel", reason)}
            onSendNotification={handleSendNotification}
            isLoading={isActionLoading}
          />

          {/* Anomaly Alerts */}
          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Cảnh báo bất thường
            </h3>
            <div className="space-y-2">
              {totalBids > 50 && (
                <div className="bg-amber-50 border border-amber-200 rounded-xl p-3">
                  <p className="text-xs font-semibold text-amber-800 uppercase tracking-[0.16em]">
                    Số lượng bid cao bất thường
                  </p>
                  <p className="text-xs text-amber-700">
                    {totalBids} bids - kiểm tra spam
                  </p>
                </div>
              )}
              {isPaused && (
                <div className="bg-sky-50 border border-sky-200 rounded-xl p-3">
                  <p className="text-xs font-semibold text-sky-800 uppercase tracking-[0.16em]">
                    Phiên đang tạm dừng
                  </p>
                  <p className="text-xs text-sky-700">
                    Admin đã tạm dừng phiên đấu giá
                  </p>
                </div>
              )}
              {totalBids === 0 && auctionStatus === "IN_PROGRESS" && (
                <div className="bg-slate-50 border border-slate-200 rounded-xl p-3">
                  <p className="text-xs font-semibold text-slate-600 uppercase tracking-[0.16em]">
                    Chưa có bid nào
                  </p>
                </div>
              )}
              {totalBids <= 50 && !isPaused && totalBids > 0 && (
                <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-3">
                  <p className="text-xs font-semibold text-emerald-800 uppercase tracking-[0.16em]">
                    Hoạt động bình thường
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
