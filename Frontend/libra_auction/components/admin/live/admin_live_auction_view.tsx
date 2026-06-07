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
import type { LiveNotification } from "@/types/notification/live_notification";

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

type LiveNotificationItem = LiveNotification;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

export default function AdminLiveAuctionView({
  auction,
  backendServerUrl,
  initialNotifications = [],
}: {
  auction: Auction;
  backendServerUrl: string;
  initialNotifications?: LiveNotificationItem[];
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
  const [participantCount] = useState(auction.total_participants || 0);
  const [highestBidder, setHighestBidder] = useState("--");
  const [notifications, setNotifications] = useState<LiveNotificationItem[]>(
    initialNotifications
  );
  const [isActionLoading, setIsActionLoading] = useState(false);

  const addNotification = useCallback((content: string, sentAt?: string) => {
    setNotifications((prev) => [
      {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
        auctionId: auction.auction_id,
        content,
        sentAt: sentAt || new Date().toISOString(),
      },
      ...prev.slice(0, 49),
    ]);
  }, [auction.auction_id]);

  const formatNotificationTime = (value: string) =>
    new Date(value).toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });

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
        setTotalBids((prev) => prev + 1);
        if (bid.bidderName) {
          setHighestBidder(bid.bidderName);
        }
        setBids((prev) => [
          {
            bidderName: bid.bidderName || "Unknown",
            amount: bid.bidAmount!,
            time: bid.bidTime
              ? new Date(bid.bidTime).toLocaleTimeString("en-US")
              : new Date().toLocaleTimeString("en-US"),
            status: (bid.status as BidEntry["status"]) || "SUCCESS",
          },
          ...prev,
        ]);
      }
    });

    auctionSocket.subscribe(statusTopic, (body: unknown) => {
      if (!isRecord(body)) return;
      const update = body as StatusUpdate;
      if (update.status) {
        setAuctionStatus(update.status);
      }
      if (update.message) {
        addNotification(update.message, update.timestamp);
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
        addNotification(msg.message);
      }
    });

    return () => {
      auctionSocket.unsubscribe(bidsTopic);
      auctionSocket.unsubscribe(statusTopic);
      auctionSocket.unsubscribe(genericTopic);
      auctionSocket.unsubscribe(adminTopic);
    };
  }, [auction.auction_id, backendServerUrl, addNotification]);

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
  };

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
              # {auction.auction_id}
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
                  ID {auction.auction_id} • {auction.category_name} • Quantity {auction.quantity}
                </p>
                <p className="text-sm leading-relaxed text-[#5A7184] line-clamp-3">
                  {auction.description}
                </p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5A7184]">Starting price</p>
                    <p className="mt-2 text-lg font-bold text-[#19A7CE]">
                      {CurrencyFormat(auction.starting_price)}
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5A7184]">Minimum increment</p>
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
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#5A7184]">Time remaining</p>
                <p className="mt-1 text-sm text-[#5A7184]">Updates based on the auction status</p>
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
              Bid history ({totalBids} bids)
            </h3>
            <BidHistory bids={bids} maxHeight="20rem" />
          </div>

          {/* Notifications Log */}
          <div className="bg-white rounded-2xl border border-[#AFD3E2] p-6 shadow-sm shadow-[#AFD3E2]/20">
            <h3 className="text-lg font-bold text-[#146C94] mb-4">
              Notification log
            </h3>
            <div className="max-h-80 overflow-auto space-y-1">
              {notifications.length === 0 ? (
                <p className="text-sm text-[#5A7184]">No notifications yet</p>
              ) : (
                notifications.map((note, idx) => (
                  <p
                    key={idx}
                    className="text-xs text-[#5A7184] border-b border-gray-100 py-1"
                  >
                    <span className="font-semibold text-[#146C94]">
                      {formatNotificationTime(note.sentAt)}
                    </span>
                    <span className="mx-2 text-[#AFD3E2]">•</span>
                    {note.content}
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

        </div>
      </div>
    </div>
  );
}
