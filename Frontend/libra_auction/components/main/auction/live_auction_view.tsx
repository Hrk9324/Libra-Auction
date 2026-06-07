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

type StatusUpdate = {
  type?: string;
  auctionId?: string;
  status?: string;
  message?: string;
  newEndTime?: string | number;
  remainingTime?: string | number;
  timestamp?: string;
  winnerId?: string;
  winnerName?: string;
  winningPrice?: number;
};

type BidUpdate = {
  auctionId?: string;
  bidAmount?: number;
  bidderId?: string;
  bidderName?: string;
  bidTime?: string;
  status?: string;
};

type AdminNotification = {
  type?: string;
  message?: string;
  timestamp?: string;
};

type UserRole = "user" | "admin";

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

  // Fetch dữ liệu lịch sử cũ khi load trang (Sửa lỗi mất Người giữ giá cao nhất)
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

  // Kênh truyền WebSocket đồng bộ tên thật
  useEffect(() => {
    const bidsTopic = `/topic/auction/${auction.auction_id}/bids`;
    const statusTopic = `/topic/auction/${auction.auction_id}/status`;
    const adminTopic = `/topic/auction/${auction.auction_id}/admin`;

    auctionSocket.connect(backendServerUrl);

    auctionSocket.subscribe(bidsTopic, (bid: any) => {
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

    auctionSocket.subscribe(statusTopic, (update: any) => {
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

    auctionSocket.subscribe(adminTopic, (msg: any) => {
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

  const isEnded = auctionStatus === "ENDED" || auctionStatus === "CANCELLED";
  const isPaused = auctionStatus === "PAUSED";
  const isLoggedIn = !!currentUserId;

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
                <p className="text-2xl font-bold mb-2">Chúc mừng! Bạn đã thắng đấu giá!</p>
                <p className="text-lg">Giá thắng: <span className="font-bold">{CurrencyFormat(winningPrice || currentBid)}</span></p>
              </>
            ) : (
              <>
                <p className="text-lg font-semibold">Người thắng: {winnerName || "Ẩn danh"}</p>
                <p className="text-sm mt-1">Giá thắng: {CurrencyFormat(winningPrice || currentBid)}</p>
              </>
            )}
          </div>
        )}

        {isEnded && !winnerId && totalBids === 0 && (
          <div className="mb-6 rounded-2xl p-5 text-center bg-gray-50 text-gray-600 border border-gray-200">
            <p className="text-lg font-semibold">Phiên đấu giá kết thúc không có lượt đặt giá</p>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 p-8 lg:p-12">
            
            {/* Cột trái: Sản phẩm & Thông báo */}
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

              <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                <p className="text-lg font-semibold text-gray-800 mb-3">Thông báo phiên chạy</p>
                <div className="max-h-60 overflow-auto space-y-2">
                  {notifications.length === 0 ? (
                    <p className="text-sm text-gray-400">Chưa có thông báo nào</p>
                  ) : (
                    notifications.map((note, idx) => (
                      <p key={idx} className="text-sm text-gray-500 border-b border-gray-100 py-2">
                        <span className="font-semibold text-gray-700">{formatNotificationTime(note.sentAt)}</span> - {note.content}
                      </p>
                    ))
                  )}
                </div>
              </div>
            </div>

            {/* Cột phải: Đồng hồ & Lịch sử trả giá */}
            <aside className="flex flex-col gap-6 h-full">
              <div className="bg-gradient-to-br from-[#146C94] to-[#19A7CE] rounded-2xl p-8">
                <p className="text-sm text-white/70 uppercase tracking-wider font-medium">Giá hiện tại</p>
                <p className="text-4xl font-bold text-white mt-2">{CurrencyFormat(currentBid)}</p>
                <p className="text-sm text-white/60 mt-3">Bước giá tối thiểu: {CurrencyFormat(auction.min_bid_increment)}</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                  <p className="text-sm text-gray-500 mb-2">Người giữ giá cao nhất</p>
                  <p className="text-xl font-bold text-gray-900 truncate">{highestBidder}</p>
                  {isHighestBidder && <p className="text-xs text-green-600 font-semibold mt-1">(Bạn đang dẫn đầu)</p>}
                </div>
                <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                  <p className="text-sm text-gray-500 mb-2">Thời gian còn lại</p>
                  <AuctionTimer endTimeMs={endTimeMs} remainingTimeMs={remainingTimeMs} isPaused={isPaused} size="sm" />
                </div>
              </div>

              {role === "user" && canBid && (
                <div className="bg-[#146C94]/10 border border-[#146C94]/5 rounded-2xl p-6 relative overflow-hidden">
                  <label className="block text-lg font-semibold text-gray-900 mb-1">Đặt giá của bạn</label>
                  <p className="text-sm text-gray-500 mb-5">Giá tối thiểu: <span className="font-semibold text-[#19A7CE]">{CurrencyFormat(minimumBid)}</span></p>
                  <div className="flex gap-3">
                    <input type="number" min={minimumBid} value={bidValue} onChange={(e) => setBidValue(e.target.value)} placeholder={String(minimumBid)} className="flex-1 bg-white border border-gray-200 rounded-xl px-5 py-4 outline-none text-base" />
                    <button onClick={handleBidClick} disabled={isBidding || !bidValue} className="bg-[#19A7CE] text-white px-8 py-4 rounded-xl font-bold hover:bg-[#146C94] transition disabled:opacity-50 text-base">{isBidding ? "Đang đặt..." : "Đặt giá"}</button>
                  </div>
                </div>
              )}

              {/* Lịch sử trả giá (Sửa triệt để lỗi No bids yet nhờ kiểm tra mảng bids động) */}
              <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100 flex-1 min-h-0 flex flex-col">
                <p className="text-lg font-semibold text-gray-800 mb-3 flex-shrink-0">Lịch sử trả giá ({totalBids} lượt)</p>
                <div className="flex-1 min-h-0 overflow-auto">
                  <BidHistory bids={bids} maxHeight="100%" currentUserId={currentUserId} />
                </div>
              </div>
            </aside>

          </div>
        </div>
      </div>

      {/* Dialog xác nhận đặt giá */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-md w-full mx-4 shadow-2xl">
            <h4 className="text-2xl font-bold text-[#146C94] mb-3">Xác nhận đặt giá</h4>
            <p className="text-base text-gray-600 mb-2">Bạn có chắc muốn đặt giá:</p>
            <p className="text-3xl font-bold text-[#19A7CE] mb-8">{CurrencyFormat(Number(bidValue))}</p>
            <div className="flex gap-4 justify-end">
              <button onClick={() => setShowConfirm(false)} className="px-6 py-3 text-gray-600 bg-gray-100 rounded-xl font-semibold hover:bg-gray-200 transition">Hủy</button>
              <button onClick={confirmBid} className="px-8 py-3 bg-[#19A7CE] text-white rounded-xl font-bold hover:bg-[#146C94] transition">Xác nhận</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}