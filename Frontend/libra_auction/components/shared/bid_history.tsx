"use client";

import { CurrencyFormat } from "@/utils/currency_format";

export interface BidEntry {
  bidderName: string;
  amount: number;
  time: string;
  status: "SUCCESS" | "ERROR" | "WINNER";
}

interface BidHistoryProps {
  bids: BidEntry[];
  maxHeight?: string;
}

export default function BidHistory({
  bids,
  maxHeight = "400px",
}: BidHistoryProps) {
  if (bids.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-sm text-[#5A7184]">Chưa có lượt đặt giá nào</p>
      </div>
    );
  }

  return (
    <div
      className="overflow-auto space-y-2 pr-1 h-full"
      style={maxHeight === "100%" ? { height: "100%" } : { maxHeight }}
    >
      {bids.map((bid, idx) => (
        <div
          key={idx}
          className={`flex items-center justify-between p-3 rounded-lg border ${
            bid.status === "WINNER"
              ? "bg-yellow-50 border-yellow-200"
              : bid.status === "ERROR"
              ? "bg-red-50 border-red-200"
              : "bg-[#F6FBFC] border-[#AFD3E2]/50"
          }`}
        >
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold text-[#146C94] truncate">
                {bid.bidderName}
              </span>
              {bid.status === "WINNER" && (
                <span className="px-1.5 py-0.5 bg-yellow-400 text-yellow-900 text-xs font-bold rounded">
                  THẮNG
                </span>
              )}
              {bid.status === "ERROR" && (
                <span className="px-1.5 py-0.5 bg-red-400 text-white text-xs font-bold rounded">
                  LỖI
                </span>
              )}
            </div>
            <p className="text-xs text-[#5A7184] mt-0.5">{bid.time}</p>
          </div>
          <div className="text-right flex-shrink-0 ml-4">
            <p
              className={`text-sm font-bold ${
                bid.status === "ERROR"
                  ? "text-red-500 line-through"
                  : bid.status === "WINNER"
                  ? "text-yellow-700"
                  : "text-[#19A7CE]"
              }`}
            >
              {CurrencyFormat(bid.amount)}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
