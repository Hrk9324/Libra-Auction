"use client";

import Link from "next/link";
import { ImageGallery } from "../product/image_gallery";
import { Auction } from "@/types/auction/auction";
import { ApprovalStatus, AuctionStatus } from "@/types/status";
import { CurrencyFormat } from "@/utils/currency_format";
import { DateFormat } from "@/utils/date_format";

interface AuctionDetailProps {
  data: Auction
}

const approvalStatusConfig: Record<ApprovalStatus, { label: string; classes: string }> = {
  PENDING: { label: "Pending", classes: "bg-amber-50 text-amber-700 border-amber-100" },
  APPROVED: { label: "Approved", classes: "bg-emerald-50 text-emerald-700 border-emerald-100" },
  REJECTED: { label: "Rejected", classes: "bg-red-50 text-red-700 border-red-100" },
};

const auctionStatusConfig: Record<AuctionStatus, { label: string; classes: string }> = {
  NOT_STARTED: { label: "Upcoming", classes: "bg-blue-50 text-blue-600 border-blue-100" },
  IN_PROGRESS: { label: "Live", classes: "bg-green-50 text-green-600 border-green-100" },
  PAUSED: { label: "Paused", classes: "bg-yellow-50 text-yellow-600 border-yellow-100" },
  ENDED: { label: "Ended", classes: "bg-gray-50 text-gray-500 border-gray-100" },
  CANCELLED: { label: "Cancelled", classes: "bg-red-50 text-red-600 border-red-100" },
};

export const AuctionDetail = ({ data }: AuctionDetailProps) => {
  const isLive = data.auction_status === "IN_PROGRESS"
  const hasImages = data.images && data.images.length > 0;
  const attributeCount = data.attributes?.length ?? 0;

  const auctionInfoItems = [
    { label: "Auction ID", value: `#${data.auction_id}` },
    { label: "Start time", value: DateFormat(data.start_time) },
    { label: "Duration", value: `${Math.floor(data.duration / 60)} minutes` },
    { label: "Starting price", value: CurrencyFormat(data.starting_price) },
    { label: "Deposit", value: CurrencyFormat(data.deposit_amount) },
    { label: "Minimum bid increment", value: CurrencyFormat(data.min_bid_increment) },
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 bg-white p-6 md:p-10 rounded-3xl border border-gray-100 shadow-sm">
        {/* Left column: gallery */}
        <ImageGallery images={hasImages ? data.images : ["/placeholder-product.png"]} />

        {/* Right column: information */}
        <div className="flex flex-col">
          <nav className="text-xs font-bold text-(--primary-color) uppercase tracking-widest mb-2">
            {data.category_name}
          </nav>

          <h1 className="text-3xl font-bold text-gray-800 mb-4">{data.product_name}</h1>

            <div className="flex flex-wrap items-center gap-2 mb-6 pb-6 border-b border-gray-50">
            <span>
              Status: {" "}
            </span>
              <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${approvalStatusConfig[data.approval_status]?.classes ?? "bg-gray-50 text-gray-500 border-gray-100"}`}>
              {approvalStatusConfig[data.approval_status]?.label ?? data.approval_status ?? "Unknown"}
            </span>
              {data.approval_status === "APPROVED" && (
                <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${auctionStatusConfig[data.auction_status]?.classes ?? "bg-gray-50 text-gray-500 border-gray-100"}`}>
                  {auctionStatusConfig[data.auction_status]?.label ?? data.auction_status ?? "Unknown"}
                </span>
              )}
          </div>

            <div className="mb-8">
              <h3 className="text-sm font-bold text-gray-800 mb-2">Auction description</h3>
              <p className="text-gray-600 text-sm leading-relaxed">{data.description}</p>
            </div>

            <div className="mb-4">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Product information</h3>
              <div className="flex items-center gap-6 mb-6 pb-6 border-b border-gray-50">
                <div>
                  <p className="text-[10px] text-gray-400 uppercase font-bold">Stock quantity</p>
                  <p className="text-xl font-bold text-gray-800">{data.quantity} units</p>
                </div>
                <div>
                  <p className="text-[10px] text-gray-400 uppercase font-bold">Product ID</p>
                  <p className="text-sm font-mono mt-1">#{data.product_id}</p>
                </div>
              </div>
            </div>

            <div className="mb-6">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Product attributes</h3>
              <div className={`grid grid-cols-1 gap-3 ${attributeCount > 1 ? "sm:grid-cols-2" : ""}`}>
                {data.attributes && data.attributes.length > 0 ? (
                  data.attributes.map((attr, idx) => (
                    <div key={`${attr.key}-${idx}`} className="flex justify-between p-3 bg-(--background-color) rounded-xl border border-gray-50">
                      <span className="text-xs text-gray-500">{attr.key}</span>
                      <span className="text-xs font-bold text-gray-700 wrap-break-word">{attr.value}</span>
                    </div>
                  ))
                ) : (
                  <div className="text-gray-400 text-sm col-span-full">
                    No attributes available.
                  </div>
                )}
              </div>
            </div>

            <div className="mb-8">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Auction details</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {auctionInfoItems.map((item) => (
                  <div key={item.label} className="flex justify-between p-3 bg-(--background-color) rounded-xl border border-gray-50 gap-4">
                    <span className="text-xs text-gray-500 shrink-0">{item.label}</span>
                    <span className="text-xs font-bold text-gray-700 text-right wrap-break-word">{item.value}</span>
                  </div>
                ))}
              </div>
            </div>

            {isLive && (
              <div className="mb-8">
                <h3 className="text-sm font-bold text-gray-800 mb-3">Live auction stats</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Current price</p>
                    <p className="mt-1 text-lg font-bold text-(--secondary-color)">{CurrencyFormat(data.current_price)}</p>
                  </div>
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Total bids</p>
                    <p className="mt-1 text-lg font-bold text-gray-800">{data.total_bids}</p>
                  </div>
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Participants</p>
                    <p className="mt-1 text-lg font-bold text-gray-800">{data.total_participants}</p>
                  </div>
                </div>
              </div>
            )}

          {/* Action Buttons */}
          <div className="flex gap-3 mt-auto pt-4 border-t border-gray-100">
            {isLive ? (
              <Link
                href={`/seller-dashboard/auctions/${data.auction_id}/live`}
                className="flex-1 flex justify-center items-center gap-2 bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-xl transition-all active:scale-95"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 3l14 9-14 9V3z" /></svg>
                View live
              </Link>
            ) : (
              <>
                <Link
                  href={`/seller-dashboard/auctions/${data.auction_id}/edit`}
                  className="flex-1 flex justify-center items-center gap-2 bg-(--primary-color) hover:bg-(--secondary-color) text-white font-bold py-3 rounded-xl transition-all active:scale-95"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /><path d="m15 5 4 4" /></svg>
                  Edit
                </Link>
                <Link
                  href={`/seller-dashboard/auctions/${data.auction_id}/delete`}
                  className="px-6 flex justify-center items-center gap-2 bg-red-50 text-red-600 hover:bg-red-100 font-bold rounded-xl transition-all active:scale-95 border border-red-100"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18" /><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" /><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" /></svg>
                  Delete
                </Link>
              </>
            )}
          </div>
        </div>
    </div>
  );
};