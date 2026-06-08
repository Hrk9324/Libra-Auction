"use client";

import Image from "next/image";
import { useEffect, useState, useCallback } from "react";
import { Auction } from "@/types/auction/auction";
import { CurrencyFormat } from "@/utils/currency_format";
import { auctionSocket } from "@/services/auction_socket";
import { fetchAuctionBids } from "@/services/fetch_auction_bids";
import BidHistory, { BidEntry } from "@/components/shared/bid_history";
import AuctionTimer from "@/components/shared/auction_timer";
import type { LiveNotification } from "@/types/notification/live_notification";

type UserRole = "user" | "admin";

interface BidSocketResponse {
  status?: "SUCCESS" | "ERROR";
  bidderId?: string;
  bidderName?: string;
  bidAmount?: string | number;
  bidTime?: string;
}

interface StatusSocketResponse {
  type?: "AUCTION_EXTENDED";
  newEndTime?: string;
  message?: string;
  status?: "IN_PROGRESS" | "PAUSED" | "ENDED" | "CANCELLED";
  remainingTime?: string | number;
  winnerId?: string;
  winnerName?: string;
  winningPrice?: number;
}

interface AdminSocketResponse {
  message?: string;
  timestamp?: string;
}

function normalizeSpringIsoDate(value: string) {
  return value.replace(/\.(\d{3})\d+(Z|[+-]\d{2}:?\d{2})$/, '.$1$2');
}

function toTimestamp(value: Date | string | number | undefined) {
  if (value instanceof Date) return value.getTime();
  if (typeof value === "number") return value;
  if (typeof value === "string") return new Date(normalizeSpringIsoDate(value)).getTime();
  return NaN;
}

function formatTime(value?: string) {
  const timestamp = toTimestamp(value);
  return Number.isNaN(timestamp)
    ? new Date().toLocaleTimeString("vi-VN")
    : new Date(timestamp).toLocaleTimeString("vi-VN");
}

function toNumber(value: string | number | undefined) {
  if (typeof value === "number") return value;
  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isNaN(parsed) ? undefined : parsed;
  }
  return undefined;
}

export default function LiveAuctionView({
  auction,
  backendServerUrl,
  role = "user",
  isRegistered = false,
  isCreator = false,
  initialNotifications = [],
  currentUserId = null,
}: {
  auction: Auction;
  backendServerUrl: string;
  role?: UserRole;
  isRegistered?: boolean;
  isCreator?: boolean;
  initialNotifications?: LiveNotification[];
  currentUserId?: string | null;
}) {
  const [currentBid, setCurrentBid] = useState<number>(
    auction.current_price || auction.starting_price || 0
  );
  const [endTimeMs, setEndTimeMs] = useState(() => {
    const explicitEndTimeMs = toTimestamp(auction.end_time);
    if (!Number.isNaN(explicitEndTimeMs)) return explicitEndTimeMs;

    const startTimeMs = toTimestamp(auction.start_time);
    return startTimeMs + auction.duration * 1000;
  });
  const [remainingTimeMs, setRemainingTimeMs] = useState<number | null>(
    auction.auction_status === "PAUSED" ? auction.remaining_time ?? null : null
  );
  const [auctionStatus, setAuctionStatus] = useState<string>(
    auction.auction_status || "IN_PROGRESS"
  );
  const [bidValue, setBidValue] = useState<string>("");
  const [isBidding, setIsBidding] = useState(false);
  const [bids, setBids] = useState<BidEntry[]>([]);
  const [totalBids, setTotalBids] = useState(auction.total_bids || 0);
  const [highestBidder, setHighestBidder] = useState("--");
  const [isHighestBidder, setIsHighestBidder] = useState(false);
  const [notifications, setNotifications] = useState<LiveNotification[]>(
    initialNotifications
  );
  const [showConfirm, setShowConfirm] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const [winnerId, setWinnerId] = useState<string | undefined>(auction.winner_id);
  const [winnerName, setWinnerName] = useState<string | undefined>(auction.winner_name);
  const [winningPrice, setWinningPrice] = useState<number | undefined>(auction.winning_price);

  useEffect(() => {
    fetchAuctionBids(auction.auction_id).then((fetchedBids) => {
      if (fetchedBids && fetchedBids.length > 0) {
        const mappedBids: BidEntry[] = fetchedBids.map((b) => ({
          bidderName: b.bidderName || "Ẩn danh",
          amount: b.bidAmount,
          time: b.bidTime ? formatTime(b.bidTime) : new Date().toLocaleTimeString("vi-VN"),
          status: (b.status as BidEntry["status"]) || "SUCCESS",
          bidderId: b.bidderId,
        }));
        setBids(mappedBids);
        setTotalBids(mappedBids.length);

        // Lấy thông tin lượt bid mới nhất đặt lên đầu ô thông tin
        const latestBid = fetchedBids[0];
        const isIHoldHighest = currentUserId && latestBid.bidderId === currentUserId;
        setHighestBidder(latestBid.bidderName || "Ẩn danh");
        setCurrentBid(latestBid.bidAmount);
        setIsHighestBidder(!!isIHoldHighest);
      }
    });
  }, [auction.auction_id, currentUserId]);

  const addNotification = useCallback((content: string, sentAt?: string) => {
    setNotifications((prev) => [
      {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
        auctionId: auction.auction_id,
        content,
        sentAt: sentAt || new Date().toISOString(),
      },
      ...prev.slice(0, 29),
    ]);
  }, [auction.auction_id]);

  const formatNotificationTime = (value: string) => {
    const timestamp = toTimestamp(value);
    return (Number.isNaN(timestamp) ? new Date() : new Date(timestamp)).toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  };

  const minimumBid = currentBid + (auction.min_bid_increment || 0);

  useEffect(() => {
    const bidsTopic = `/topic/auction/${auction.auction_id}/bids`;
    const statusTopic = `/topic/auction/${auction.auction_id}/status`;
    const adminTopic = `/topic/auction/${auction.auction_id}/admin`;

    auctionSocket.connect(backendServerUrl);

    auctionSocket.subscribe(bidsTopic, (bid: BidSocketResponse) => {
      if (!bid) return;

      if (bid.status === "ERROR") {
        addNotification(`Lỗi: ${bid.bidderName || "Không thể đặt giá"}`);
        return;
      }

      const bidAmount = Number(bid.bidAmount);
      if (!isNaN(bidAmount) && bidAmount > 0) {
        setCurrentBid(bidAmount);

        const displayName = bid.bidderName || "Ẩn danh";
        const isMyBid = currentUserId && bid.bidderId && bid.bidderId === currentUserId;

        setBids((prev) => [
          {
            bidderName: displayName,
            amount: bidAmount,
            time: bid.bidTime ? formatTime(bid.bidTime) : new Date().toLocaleTimeString("vi-VN"),
            status: "SUCCESS",
            bidderId: bid.bidderId,
          },
          ...prev,
        ]);
        setTotalBids((prev) => prev + 1);
        setHighestBidder(displayName);
        setIsHighestBidder(!!isMyBid);

        addNotification(`${isMyBid ? "Bạn" : displayName} đã đặt giá ${CurrencyFormat(bidAmount)}`);
      }
    });

    auctionSocket.subscribe(statusTopic, (update: StatusSocketResponse) => {
      if (!update) return;

      if (update.type === "AUCTION_EXTENDED" || update.newEndTime) {
        const newEndTimeMs = toTimestamp(update.newEndTime);
        if (!Number.isNaN(newEndTimeMs) && newEndTimeMs > 0) {
          setEndTimeMs(newEndTimeMs);
        }
        addNotification(update.message || "Phiên đấu giá đã được gia hạn thêm 5 phút");
        return;
      }

      if (update.status) {
        setAuctionStatus(update.status);
        switch (update.status) {
          case "PAUSED": {
            const parsedRemainingTime = toNumber(update.remainingTime);
            if (parsedRemainingTime !== undefined) {
              setRemainingTimeMs(parsedRemainingTime);
            }
            setStatusMessage("Phiên đấu giá đang tạm dừng. Vui lòng chờ admin tiếp tục.");
            addNotification("Phiên đấu giá đã bị tạm dừng");
            break;
          }
          case "IN_PROGRESS":
            setRemainingTimeMs(null);
            setStatusMessage(null);
            addNotification("Phiên đấu giá đã tiếp tục");
            break;
          case "ENDED":
            setStatusMessage("Phiên đấu giá đã kết thúc.");
            setIsHighestBidder(false);
            if (update.winnerId) setWinnerId(update.winnerId);
            if (update.winnerName) setWinnerName(update.winnerName);
            if (typeof update.winningPrice === "number") setWinningPrice(update.winningPrice);
            addNotification("Phiên đấu giá đã kết thúc");
            break;
          case "CANCELLED":
            setStatusMessage("Phiên đấu giá đã bị hủy.");
            addNotification("Phiên đấu giá đã bị hủy");
            break;
        }
      }
    });

    auctionSocket.subscribe(adminTopic, (msg: AdminSocketResponse) => {
      if (msg && msg.message) {
        addNotification(`[Admin] ${msg.message}`, msg.timestamp);
      }
    });

    return () => {
      auctionSocket.unsubscribe(bidsTopic);
      auctionSocket.unsubscribe(statusTopic);
      auctionSocket.unsubscribe(adminTopic);
    };
  }, [auction.auction_id, backendServerUrl, currentUserId, addNotification]);

  const handleBidClick = () => {
    const value = Number(bidValue);
    if (Number.isNaN(value) || value <= 0 || value < minimumBid) {
      addNotification(`Giá đặt phải tối thiểu ${CurrencyFormat(minimumBid)}`);
      return;
    }
    setShowConfirm(true);
  };

  const confirmBid = async () => {
    const value = Number(bidValue);
    if (Number.isNaN(value) || value <= 0 || value < minimumBid) {
      addNotification(`Giá đặt phải tối thiểu ${CurrencyFormat(minimumBid)}`);
      return;
    }
    setShowConfirm(false);
    setIsBidding(true);
    try {
      auctionSocket.send("/app/bid", {
        auctionId: auction.auction_id,
        bidAmount: value,
        bidderId: currentUserId || undefined,
      });
      setBidValue("");
      addNotification(`Đã gửi yêu cầu đặt giá ${CurrencyFormat(value)}, đang chờ xác nhận...`);
    } catch (err) {
      console.error(err);
      addNotification("Lỗi khi gửi đặt giá");
    } finally {
      setIsBidding(false);
    }
  };

  const canBid =
    role === "user" &&
    auctionStatus === "IN_PROGRESS" &&
    !!currentUserId &&
    isRegistered &&
    !isCreator;

  const bidDisabledReason =
    role === "admin"
      ? "Admins can monitor the auction but cannot place bids."
      : !currentUserId
      ? "Please sign in to place a bid."
      : isCreator
      ? "You cannot place a bid because you are the seller."
      : !isRegistered
      ? "Register for this auction before placing a bid."
      : auctionStatus === "PAUSED"
      ? "This auction is paused. Please wait until it resumes."
      : auctionStatus !== "IN_PROGRESS"
      ? "Bids can only be placed while the auction is live."
      : null;

  const isEnded = auctionStatus === "ENDED" || auctionStatus === "CANCELLED";
  const isPaused = auctionStatus === "PAUSED";

  return (
    <div className="min-h-screen bg-gray-50/50 pt-10 px-16 pb-12">
      <div className="mx-auto">
        {statusMessage && (
          <div className={`mb-6 rounded-2xl p-5 text-center font-semibold text-lg ${
            isPaused ? "bg-yellow-50 text-yellow-800 border border-yellow-200" : "bg-gray-100 text-gray-600 border border-gray-200"
          }`}>{statusMessage}</div>
        )}

        {isEnded && winnerId && (
          <div className={`mb-6 rounded-2xl p-6 text-center border ${
            currentUserId && winnerId === currentUserId ? "bg-emerald-50 text-emerald-800 border-emerald-200" : "bg-gray-50 text-gray-600 border-gray-200"
          }`}>
            {currentUserId && winnerId === currentUserId ? (
              <>
                <p className="text-2xl font-bold mb-2">Congratulations! You won this auction!</p>
                <p className="text-lg">Winning price: <span className="font-bold">{CurrencyFormat(winningPrice || currentBid)}</span></p>
              </>
            ) : (
              <>
                <p className="text-lg font-semibold">Winner: {winnerName || "Anonymous"}</p>
                <p className="text-sm mt-1">Winning price: {CurrencyFormat(winningPrice || currentBid)}</p>
              </>
            )}
          </div>
        )}

        {isEnded && !winnerId && totalBids === 0 && (
          <div className="mb-6 rounded-2xl p-5 text-center bg-gray-50 text-gray-600 border border-gray-200">
            <p className="text-lg font-semibold">This auction ended without any bids</p>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 p-8 lg:p-12">
            
            {/* Left column: Product & notifications */}
            <div className="flex flex-col gap-6">
              <div className="relative aspect-square w-full rounded-2xl bg-gray-50 border border-gray-100 overflow-hidden shadow-sm flex items-center justify-center">
                <Image src={auction.images?.[0] || "/placeholder-image.jpg"} alt={auction.product_name} fill className="object-contain p-4" />
                <div className="absolute top-4 left-4">
                  <span className={`inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-bold text-white ${
                    auctionStatus === "IN_PROGRESS" ? "bg-red-500" : auctionStatus === "PAUSED" ? "bg-yellow-500" : "bg-gray-500"
                  }`}>
                    {auctionStatus === "IN_PROGRESS" && (
                      <span className="relative flex h-2.5 w-2.5">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75" />
                        <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-white" />
                      </span>
                    )}
                    {auctionStatus}
                  </span>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm text-gray-500 font-medium uppercase tracking-wider">
                  <span>{auction.category_name}</span>
                  <span>#{auction.product_id}</span>
                </div>
                <h1 className="text-3xl font-bold text-gray-900 leading-tight">{auction.product_name}</h1>
                <p className="text-base text-gray-500 line-clamp-3">{auction.description}</p>
              </div>

              <div className="bg-[#F2F8FA]/50 rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
                <h3 className="text-lg font-bold text-[#146C94] mb-4">Session notifications</h3>
                <div className="max-h-60 overflow-auto space-y-1">
                  {notifications.length === 0 ? (
                    <p className="text-sm text-[#5A7184]">No notifications yet</p>
                  ) : (
                    notifications.map((note, idx) => (
                      <p key={idx} className="text-xs text-[#5A7184] border-b border-gray-100 py-1">
                        <span className="font-semibold text-[#146C94]">{formatNotificationTime(note.sentAt)}</span>
                        <span className="mx-2 text-[#AFD3E2]">•</span>
                        {note.content}
                      </p>
                    ))
                  )}
                </div>
              </div>
            </div>

            <aside className="flex flex-col gap-6 h-full">
              <div className="bg-gradient-to-br from-[#146C94] to-[#19A7CE] rounded-2xl p-8">
                <p className="text-sm text-white/70 uppercase tracking-wider font-medium">Current price</p>
                <p className="text-4xl font-bold text-white mt-2">{CurrencyFormat(currentBid)}</p>
                <p className="text-sm text-white/60 mt-3">Minimum increment: {CurrencyFormat(auction.min_bid_increment)}</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="rounded-2xl border border-[#AFD3E2] bg-[#F2F8FA]/50 p-5 text-center shadow-sm shadow-[#AFD3E2]/20">
                  <p className="text-sm font-semibold uppercase tracking-[0.16em] text-[#5A7184]">Highest bidder</p>
                  <div className="mt-3 flex min-h-16 flex-col items-center justify-center rounded-xl border border-[#D8EEF4] bg-[#EAF4F7] px-4 py-3">
                    <p className="max-w-full truncate text-xl font-bold text-[#146C94]">{highestBidder}</p>
                    {isHighestBidder && <p className="text-xs text-amber-700 font-semibold mt-1">You are leading</p>}
                  </div>
                </div>
                <div className="rounded-2xl border border-[#AFD3E2] bg-[#F2F8FA]/50 p-5 text-center shadow-sm shadow-[#AFD3E2]/20">
                  <p className="text-sm font-semibold uppercase tracking-[0.16em] text-[#5A7184]">Time remaining</p>
                  <div className="mt-3 flex min-h-16 items-center justify-center rounded-xl border border-[#D8EEF4] bg-[#EAF4F7] px-4 py-3 text-[#146C94]">
                    <AuctionTimer endTimeMs={endTimeMs} remainingTimeMs={remainingTimeMs} isPaused={isPaused} size="sm" />
                  </div>
                </div>
              </div>

              {role === "user" && (
                <div className="bg-[#F2F8FA]/50 rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
                  <label className="block text-lg font-bold text-[#146C94] mb-1">Place your bid</label>
                  <p className="text-sm text-[#5A7184] mb-5">Minimum bid: <span className="font-semibold text-[#19A7CE]">{CurrencyFormat(minimumBid)}</span></p>
                  {canBid ? (
                    <div className="flex gap-3">
                      <input type="number" min={minimumBid} value={bidValue} onChange={(e) => setBidValue(e.target.value)} placeholder={String(minimumBid)} className="flex-1 bg-white border border-gray-200 rounded-xl px-5 py-4 outline-none text-base" />
                      <button onClick={handleBidClick} disabled={isBidding || !bidValue} className="bg-[#19A7CE] text-white px-8 py-4 rounded-xl font-bold hover:bg-[#146C94] transition disabled:opacity-50 text-base">{isBidding ? "Placing..." : "Place bid"}</button>
                    </div>
                  ) : (
                    <div className="rounded-xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm font-medium text-amber-800">
                      {bidDisabledReason || "You cannot place a bid in this auction yet."}
                    </div>
                  )}
                </div>
              )}

              <div className="bg-[#F2F8FA]/50 rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20 flex-1 min-h-0 flex flex-col">
                <h3 className="text-lg font-bold text-[#146C94] mb-4 flex-shrink-0">Bid history ({totalBids} bids)</h3>
                <div className="flex-1 min-h-0 overflow-auto">
                  <BidHistory bids={bids} maxHeight="100%" currentUserId={currentUserId} />
                </div>
              </div>
            </aside>

          </div>
        </div>
      </div>

      {showConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-md w-full mx-4 shadow-2xl">
            <h4 className="text-2xl font-bold text-[#146C94] mb-3">Confirm bid</h4>
            <p className="text-base text-gray-600 mb-2">Are you sure you want to bid:</p>
            <p className="text-3xl font-bold text-[#19A7CE] mb-8">{CurrencyFormat(Number(bidValue))}</p>
            <div className="flex gap-4 justify-end">
              <button onClick={() => setShowConfirm(false)} className="px-6 py-3 text-gray-600 bg-gray-100 rounded-xl font-semibold hover:bg-gray-200 transition">Cancel</button>
              <button onClick={confirmBid} className="px-8 py-3 bg-[#19A7CE] text-white rounded-xl font-bold hover:bg-[#146C94] transition">Confirm</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}