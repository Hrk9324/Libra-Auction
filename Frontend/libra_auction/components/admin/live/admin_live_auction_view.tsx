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
    command: "pause" | "resume" | "end" | "cancel"
  ) => {
    setIsActionLoading(true);
    try {
      auctionSocket.send(
        `/app/admin/auction/${auction.auction_id}/${command}`,
        {
          auctionId: auction.auction_id,
          command,
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

  return (
    <div className="min-h-screen bg-[#F6F1F1] p-6">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#146C94]">
            Live Auction Monitor
          </h1>
          <p className="text-sm text-[#5A7184]">
            {auction.product_name} • #{auction.auction_id}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span
            className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-semibold ${
              auctionStatus === "IN_PROGRESS"
                ? "bg-green-100 text-green-800"
                : auctionStatus === "PAUSED"
                ? "bg-yellow-100 text-yellow-800"
                : auctionStatus === "ENDED"
                ? "bg-gray-100 text-gray-800"
                : "bg-red-100 text-red-800"
            }`}
          >
            {auctionStatus === "IN_PROGRESS" && (
              <span className="relative flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500" />
              </span>
            )}
            {auctionStatus === "PAUSED" && "⏸"}
            {auctionStatus === "ENDED" && "⏹"}
            {auctionStatus === "CANCELLED" && "✕"}
            {auctionStatus === "IN_PROGRESS"
              ? "LIVE"
              : auctionStatus === "PAUSED"
              ? "PAUSED"
              : auctionStatus === "ENDED"
              ? "ENDED"
              : "CANCELLED"}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Product Info */}
          <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm">
            <div className="flex gap-6">
              <div className="relative w-48 h-36 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0">
                <Image
                  src={auction.images?.[0] || "/placeholder-image.jpg"}
                  alt={auction.product_name}
                  fill
                  className="object-contain p-2"
                />
              </div>
              <div className="flex-1">
                <h2 className="text-xl font-bold text-[#146C94]">
                  {auction.product_name}
                </h2>
                <p className="text-sm text-[#5A7184] mt-1">
                  {auction.category_name} • SL: {auction.quantity}
                </p>
                <p className="text-sm text-[#5A7184] mt-2 line-clamp-2">
                  {auction.description}
                </p>
                <div className="mt-3 grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-xs text-[#5A7184]">Giá khởi điểm</p>
                    <p className="text-lg font-bold text-[#19A7CE]">
                      {CurrencyFormat(auction.starting_price)}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-[#5A7184]">Bước giá tối thiểu</p>
                    <p className="text-lg font-bold text-[#19A7CE]">
                      {CurrencyFormat(auction.min_bid_increment)}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Timer */}
          <div className="bg-white rounded-xl border border-[#AFD3E2] p-4 shadow-sm">
            <AuctionTimer
              endTimeMs={endTimeMs}
              isPaused={isPaused}
              size="lg"
            />
          </div>

          {/* Bid History */}
          <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Lịch sử trả giá ({totalBids} bids)
            </h3>
            <BidHistory bids={bids} maxHeight="400px" />
          </div>

          {/* Notifications Log */}
          <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm">
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
        <div className="space-y-6">
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
            onCancel={() => sendAdminCommand("cancel")}
            onSendNotification={handleSendNotification}
            isLoading={isActionLoading}
          />

          {/* Anomaly Alerts */}
          <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Cảnh báo bất thường
            </h3>
            <div className="space-y-2">
              {totalBids > 50 && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                  <p className="text-xs font-semibold text-yellow-800">
                    ⚠ Số lượng bid cao bất thường
                  </p>
                  <p className="text-xs text-yellow-600">
                    {totalBids} bids - kiểm tra spam
                  </p>
                </div>
              )}
              {isPaused && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <p className="text-xs font-semibold text-blue-800">
                    ℹ Phiên đang tạm dừng
                  </p>
                  <p className="text-xs text-blue-600">
                    Admin đã tạm dừng phiên đấu giá
                  </p>
                </div>
              )}
              {totalBids === 0 && auctionStatus === "IN_PROGRESS" && (
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-3">
                  <p className="text-xs font-semibold text-gray-600">
                    Chưa có bid nào
                  </p>
                </div>
              )}
              {totalBids <= 50 &&
                !isPaused &&
                totalBids > 0 && (
                  <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                    <p className="text-xs font-semibold text-green-800">
                      ✓ Hoạt động bình thường
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
