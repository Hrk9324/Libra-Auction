"use client";

import Image from "next/image";
import { useEffect, useState, useCallback } from "react";
import { Auction } from "@/types/auction/auction";
import { CurrencyFormat } from "@/utils/currency_format";
import { auctionSocket } from "@/services/auction_socket";
import BidHistory, { BidEntry } from "@/components/shared/bid_history";
import AuctionTimer from "@/components/shared/auction_timer";

type AuctionSocketUpdate = {
  currentPrice?: number;
  current_price?: number;
};

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

type AdminNotification = {
  type?: string;
  message?: string;
  timestamp?: string;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

type UserRole = "user" | "admin";

export default function LiveAuctionView({
  auction,
  backendServerUrl,
  role = "user",
}: {
  auction: Auction;
  backendServerUrl: string;
  role?: UserRole;
}) {
  const [currentBid, setCurrentBid] = useState<number>(
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
  const [bidValue, setBidValue] = useState<string>("");
  const [isBidding, setIsBidding] = useState(false);
  const [bids, setBids] = useState<BidEntry[]>([]);
  const [totalBids, setTotalBids] = useState(auction.total_bids || 0);
  const [highestBidder, setHighestBidder] = useState("--");
  const [isHighestBidder, setIsHighestBidder] = useState(false);
  const [notifications, setNotifications] = useState<string[]>([]);
  const [showConfirm, setShowConfirm] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const addNotification = useCallback((msg: string) => {
    setNotifications((prev) => [
      `${new Date().toLocaleTimeString("vi-VN")} - ${msg}`,
      ...prev.slice(0, 29),
    ]);
  }, []);

  const minimumBid = currentBid + (auction.min_bid_increment || 0);

  // WebSocket subscriptions
  useEffect(() => {
    const bidsTopic = `/topic/auction/${auction.auction_id}/bids`;
    const statusTopic = `/topic/auction/${auction.auction_id}/status`;
    const genericTopic = `/topic/auction/${auction.auction_id}`;
    const adminTopic = `/topic/auction/${auction.auction_id}/admin`;

    auctionSocket.connect(backendServerUrl);

    // Bid updates
    auctionSocket.subscribe(bidsTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const bid = body as unknown as BidUpdate;
      if (typeof bid.bidAmount === "number") {
        setCurrentBid(bid.bidAmount);
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
        // Check if current user is highest bidder (simplified - in production, compare bidderId)
        if (bid.bidderName) {
          setIsHighestBidder(false); // Would need current user ID to compare
        }
      }
      const price = bid.currentPrice ?? bid.current_price;
      if (typeof price === "number" && price > 0) {
        setCurrentBid(price);
      }
    });

    // Status updates
    auctionSocket.subscribe(statusTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const update = body as unknown as StatusUpdate;
      if (update.status) {
        setAuctionStatus(update.status);
      }
      if (update.type) {
        switch (update.type) {
          case "AUCTION_PAUSED":
            setAuctionStatus("PAUSED");
            setStatusMessage(
              "Phiên đấu giá đang tạm dừng. Vui lòng chờ admin tiếp tục."
            );
            addNotification("Phiên đấu giá đã bị tạm dừng");
            break;
          case "AUCTION_RESUMED":
            setAuctionStatus("IN_PROGRESS");
            setStatusMessage(null);
            addNotification("Phiên đấu giá đã tiếp tục");
            break;
          case "AUCTION_ENDED":
            setAuctionStatus("ENDED");
            setStatusMessage("Phiên đấu giá đã kết thúc.");
            addNotification("Phiên đấu giá đã kết thúc");
            break;
          case "AUCTION_CANCELLED":
            setAuctionStatus("CANCELLED");
            setStatusMessage("Phiên đấu giá đã bị hủy.");
            addNotification("Phiên đấu giá đã bị hủy");
            break;
          case "AUCTION_EXTENDED":
            addNotification("Phiên đấu giá đã được gia hạn");
            break;
        }
      }
    });

    // Generic updates
    auctionSocket.subscribe(genericTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const bid = body as unknown as BidUpdate;
      const price = bid.currentPrice ?? bid.current_price;
      if (typeof price === "number") {
        setCurrentBid(price);
      }
    });

    // Admin notifications
    auctionSocket.subscribe(adminTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const msg = body as AdminNotification;
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

  const handleBidClick = () => {
    const value = Number(bidValue);
    if (Number.isNaN(value) || value <= 0) return;
    if (value < minimumBid) {
      addNotification(
        `Giá đặt phải tối thiểu ${CurrencyFormat(minimumBid)}`
      );
      return;
    }
    setShowConfirm(true);
  };

  const confirmBid = async () => {
    const value = Number(bidValue);
    setShowConfirm(false);
    setIsBidding(true);
    try {
      auctionSocket.send("/app/bid", {
        auctionId: auction.auction_id,
        bidAmount: value,
        bidderName: "You", // Would use actual user name
      });
      setBidValue("");
      addNotification(`Đã đặt giá ${CurrencyFormat(value)}`);
    } catch (err) {
      console.error(err);
      addNotification("Lỗi khi đặt giá");
    } finally {
      setIsBidding(false);
    }
  };

  const canBid =
    role === "user" &&
    (auctionStatus === "IN_PROGRESS" || auctionStatus === "NOT_STARTED");

  const isEnded = auctionStatus === "ENDED" || auctionStatus === "CANCELLED";
  const isPaused = auctionStatus === "PAUSED";

  return (
    <div className="min-h-screen bg-gray-50/50 pt-8 px-6 md:px-16 pb-12">
      <div className="mx-auto max-w-6xl">
        {/* Status Banner */}
        {statusMessage && (
          <div
            className={`mb-4 rounded-xl p-4 text-center font-semibold ${
              isPaused
                ? "bg-yellow-50 text-yellow-800 border border-yellow-200"
                : isEnded
                ? "bg-gray-50 text-gray-600 border border-gray-200"
                : "bg-blue-50 text-blue-800 border border-blue-200"
            }`}
          >
            {statusMessage}
          </div>
        )}

        {/* Role Indicator */}
        {role === "admin" && (
          <div className="mb-4 rounded-xl p-3 bg-blue-50 text-blue-700 border border-blue-200 text-sm text-center">
            Admin - Chế độ giám sát
          </div>
        )}

        {/* Highest Bidder Indicator */}
        {role === "user" && isHighestBidder && canBid && (
          <div className="mb-4 rounded-xl p-3 bg-green-50 text-green-700 border border-green-200 text-sm text-center font-semibold">
            Bạn đang là người giữ giá cao nhất!
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 p-6">
            {/* Left Column - Product & History */}
            <div className="lg:col-span-2">
              {/* Product Image */}
              <div className="relative aspect-video w-full rounded-xl bg-gray-100 overflow-hidden">
                <Image
                  src={auction.images?.[0] || "/placeholder-image.jpg"}
                  alt={auction.product_name}
                  fill
                  className="object-contain p-6"
                />
                {/* Live Badge */}
                <div className="absolute top-4 left-4">
                  <span
                    className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold text-white ${
                      auctionStatus === "IN_PROGRESS"
                        ? "bg-red-500"
                        : auctionStatus === "PAUSED"
                        ? "bg-yellow-500"
                        : "bg-gray-500"
                    }`}
                  >
                    {auctionStatus === "IN_PROGRESS" && (
                      <span className="relative flex h-2 w-2">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75" />
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-white" />
                      </span>
                    )}
                    {auctionStatus === "IN_PROGRESS"
                      ? "LIVE"
                      : auctionStatus === "PAUSED"
                      ? "PAUSED"
                      : "OFFLINE"}
                  </span>
                </div>
              </div>

              {/* Product Info */}
              <div className="mt-4 flex items-start justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">
                    {auction.product_name}
                  </h2>
                  <p className="text-sm text-gray-500 mt-1">
                    {auction.category_name} • #{auction.product_id}
                  </p>
                  <p className="text-sm text-gray-500 mt-2 line-clamp-2">
                    {auction.description}
                  </p>
                </div>
              </div>

              {/* Timer */}
              <div className="mt-4">
                <AuctionTimer
                  endTimeMs={endTimeMs}
                  isPaused={isPaused}
                  size="md"
                />
              </div>

              {/* Bid History */}
              <div className="mt-6">
                <h3 className="text-sm font-semibold text-gray-700 mb-3">
                  Lịch sử trả giá ({totalBids} bids)
                </h3>
                <div className="rounded-xl border border-gray-100 bg-gray-50 p-3">
                  <BidHistory bids={bids} maxHeight="300px" />
                </div>
              </div>
            </div>

            {/* Right Column - Price & Bid */}
            <aside className="flex flex-col gap-4">
              {/* Current Price */}
              <div className="bg-gradient-to-r from-[#146C94] to-[#19A7CE] rounded-xl p-5">
                <p className="text-xs text-white/70 uppercase tracking-wider">
                  Giá hiện tại
                </p>
                <p className="text-3xl font-bold text-white mt-1">
                  {CurrencyFormat(currentBid)}
                </p>
                <p className="text-xs text-white/60 mt-1">
                  Bước giá tối thiểu: {CurrencyFormat(auction.min_bid_increment)}
                </p>
              </div>

              {/* Highest Bidder */}
              <div className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                <p className="text-xs text-gray-500">
                  Người giữ giá cao nhất
                </p>
                <p className="text-sm font-bold text-gray-800 mt-1">
                  {highestBidder}
                </p>
              </div>

              {/* Bid Form - Buyer Only */}
              {role === "user" && (
                <div className="bg-white rounded-xl p-4 border border-gray-100">
                  {canBid ? (
                    <>
                      <label className="block text-sm font-semibold text-gray-700">
                        Đặt giá của bạn
                      </label>
                      <p className="text-xs text-gray-400 mt-1 mb-3">
                        Tối thiểu: {CurrencyFormat(minimumBid)}
                      </p>
                      <div className="flex gap-2">
                        <input
                          type="number"
                          min={minimumBid}
                          value={bidValue}
                          onChange={(e) => setBidValue(e.target.value)}
                          placeholder={String(minimumBid)}
                          className="flex-1 border border-gray-200 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-[#19A7CE]/40 text-sm"
                        />
                        <button
                          onClick={handleBidClick}
                          disabled={isBidding || !bidValue}
                          className="bg-[#19A7CE] text-white px-5 py-3 rounded-xl font-bold hover:bg-[#146C94] transition disabled:opacity-50 disabled:cursor-not-allowed text-sm"
                        >
                          {isBidding ? "Đang đặt..." : "Place Bid"}
                        </button>
                      </div>
                    </>
                  ) : isPaused ? (
                    <div className="text-center py-4">
                      <p className="text-yellow-600 font-semibold">
                        Phiên đấu giá đang tạm dừng
                      </p>
                      <p className="text-xs text-gray-400 mt-1">
                        Không thể đặt giá lúc này
                      </p>
                    </div>
                  ) : isEnded ? (
                    <div className="text-center py-4">
                      <p className="text-gray-500 font-semibold">
                        Phiên đấu giá đã kết thúc
                      </p>
                    </div>
                  ) : null}
                </div>
              )}

              {/* Notifications */}

              {/* Notifications */}
              <div className="bg-gray-50 rounded-xl p-4 border border-gray-100 flex-1">
                <p className="text-sm font-semibold text-gray-700 mb-2">
                  Thông báo
                </p>
                <div className="max-h-48 overflow-auto space-y-1">
                  {notifications.length === 0 ? (
                    <p className="text-xs text-gray-400">Chưa có thông báo</p>
                  ) : (
                    notifications.map((note, idx) => (
                      <p
                        key={idx}
                        className="text-xs text-gray-500 border-b border-gray-100 py-1"
                      >
                        {note}
                      </p>
                    ))
                  )}
                </div>
              </div>
            </aside>
          </div>
        </div>
      </div>

      {/* Bid Confirmation Dialog */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 max-w-sm w-full mx-4 shadow-xl">
            <h4 className="text-lg font-bold text-[#146C94] mb-2">
              Xác nhận đặt giá
            </h4>
            <p className="text-sm text-gray-600 mb-1">
              Bạn có chắc muốn đặt giá:
            </p>
            <p className="text-2xl font-bold text-[#19A7CE] mb-6">
              {CurrencyFormat(Number(bidValue))}
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setShowConfirm(false)}
                className="px-4 py-2 text-gray-600 bg-gray-100 rounded-lg text-sm font-semibold hover:bg-gray-200 transition"
              >
                Hủy
              </button>
              <button
                onClick={confirmBid}
                className="px-6 py-2 bg-[#19A7CE] text-white rounded-lg text-sm font-bold hover:bg-[#146C94] transition"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
