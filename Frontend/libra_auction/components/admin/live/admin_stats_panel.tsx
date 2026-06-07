"use client";

import { CurrencyFormat } from "@/utils/currency_format";

interface AdminStatsPanelProps {
  currentPrice: number;
  endTimeMs: number;
  viewerCount: number;
  participantCount: number;
  totalBids: number;
  highestBidder: string;
  auctionStatus: string;
}

export default function AdminStatsPanel({
  currentPrice,
  viewerCount,
  participantCount,
  totalBids,
  highestBidder,
}: AdminStatsPanelProps) {
  return (
    <div className="rounded-2xl border border-[#AFD3E2] bg-white p-6 shadow-sm shadow-[#AFD3E2]/20">
      <h3 className="mb-4 text-lg font-bold text-[#146C94]">
        Thống kê thời gian thực
      </h3>

      {/* Current Price */}
      <div className="mb-4 rounded-2xl bg-linear-to-r from-[#146C94] to-[#19A7CE] p-4">
        <p className="text-xs uppercase tracking-[0.2em] text-white/70">
          Giá hiện tại
        </p>
        <p className="mt-1 text-3xl font-bold text-white">
          {CurrencyFormat(currentPrice)}
        </p>
      </div>

      {/* Highest Bidder */}
      <div className="mb-4 rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-3">
        <p className="text-xs uppercase text-[#5A7184]">
          Người giữ giá cao nhất
        </p>
        <p className="mt-1 truncate text-sm font-bold text-[#146C94]">
          {highestBidder}
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-3">
        <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-3 text-center">
          <p className="text-2xl font-bold text-[#146C94]">{viewerCount}</p>
          <p className="text-xs text-[#5A7184]">Người xem</p>
        </div>
        <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-3 text-center">
          <p className="text-2xl font-bold text-[#146C94]">
            {participantCount}
          </p>
          <p className="text-xs text-[#5A7184]">Người tham gia</p>
        </div>
        <div className="col-span-2 rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-3 text-center">
          <p className="text-2xl font-bold text-[#19A7CE]">{totalBids}</p>
          <p className="text-xs text-[#5A7184]">Tổng số bid</p>
        </div>
      </div>
    </div>
  );
}
