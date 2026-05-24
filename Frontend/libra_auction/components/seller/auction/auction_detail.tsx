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
  CHUA_DUYET: { label: "Pending", classes: "bg-amber-50 text-amber-700 border-amber-100" },
  DA_DUYET: { label: "Approved", classes: "bg-emerald-50 text-emerald-700 border-emerald-100" },
  BI_TU_CHOI: { label: "Rejected", classes: "bg-red-50 text-red-700 border-red-100" },
};

const auctionStatusConfig: Record<AuctionStatus, { label: string; classes: string }> = {
  CHUA_BAT_DAU: { label: "Upcoming", classes: "bg-blue-50 text-blue-600 border-blue-100" },
  DANG_DIEN_RA: { label: "Live", classes: "bg-green-50 text-green-600 border-green-100" },
  DA_KET_THUC: { label: "Ended", classes: "bg-gray-50 text-gray-500 border-gray-100" },
  BI_HUY: { label: "Cancelled", classes: "bg-red-50 text-red-600 border-red-100" },
};

export const AuctionDetail = ({ data }: AuctionDetailProps) => {
  const isLive = data.auction_status === "DANG_DIEN_RA"
  const hasImages = data.images && data.images.length > 0;
  const attributeCount = data.attributes?.length ?? 0;

  const auctionInfoItems = [
    { label: "Mã phiên", value: `#${data.auction_id}` },
    { label: "Thời gian bắt đầu", value: DateFormat(data.start_time) },
    { label: "Thời lượng", value: `${data.duration} phút` },
    { label: "Giá khởi điểm", value: CurrencyFormat(data.starting_price) },
    { label: "Tiền cọc", value: CurrencyFormat(data.tien_coc) },
    { label: "Bước giá tối thiểu", value: CurrencyFormat(data.min_bid_increment) },
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 bg-white p-6 md:p-10 rounded-3xl border border-gray-100 shadow-sm">
        {/* Cột trái: Gallery */}
        <ImageGallery images={hasImages ? data.images : ["/placeholder-product.png"]} />

        {/* Cột phải: Thông tin */}
        <div className="flex flex-col">
          <nav className="text-xs font-bold text-(--primary-color) uppercase tracking-widest mb-2">
            {data.category_name}
          </nav>

          <h1 className="text-3xl font-bold text-gray-800 mb-4">{data.auction_name}</h1>

            <div className="flex flex-wrap items-center gap-2 mb-6 pb-6 border-b border-gray-50">
            <span>
              Trạng thái: {" "}
              {data.approval_status === "DA_DUYET" && (
                <span className={`${isLive ? "text-green-600" : data.auction_status === "CHUA_BAT_DAU" ? "text-blue-500" : data.auction_status === "DA_KET_THUC" ? "text-gray-500" : "text-red-500"}`}>
                  {data.auction_status}
                </span>
              )}
            </span>
              <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${approvalStatusConfig[data.approval_status].classes}`}>
              {approvalStatusConfig[data.approval_status].label}
            </span>
              {data.approval_status === "DA_DUYET" && (
                <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${auctionStatusConfig[data.auction_status].classes}`}>
                  {auctionStatusConfig[data.auction_status].label}
                </span>
              )}
          </div>

            <div className="mb-8">
              <h3 className="text-sm font-bold text-gray-800 mb-2">Mô tả phiên đấu giá</h3>
              <p className="text-gray-600 text-sm leading-relaxed">{data.description}</p>
            </div>

            <div className="mb-4">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Thông tin sản phẩm</h3>
              <div className="flex items-center gap-6 mb-6 pb-6 border-b border-gray-50">
                <div>
                  <p className="text-[10px] text-gray-400 uppercase font-bold">Số lượng kho</p>
                  <p className="text-xl font-bold text-gray-800">{data.quantity} chiếc</p>
                </div>
                <div>
                  <p className="text-[10px] text-gray-400 uppercase font-bold">Mã tài sản</p>
                  <p className="text-sm font-mono mt-1">#{data.product_id}</p>
                </div>
              </div>
            </div>

            <div className="mb-6">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Thuộc tính sản phẩm</h3>
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
                    Không có thuộc tính.
                  </div>
                )}
              </div>
            </div>

            <div className="mb-8">
              <h3 className="text-sm font-bold text-gray-800 mb-3">Thông tin phiên đấu giá</h3>
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
                <h3 className="text-sm font-bold text-gray-800 mb-3">Số liệu khi đấu giá đang diễn ra</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Giá hiện tại</p>
                    <p className="mt-1 text-lg font-bold text-(--secondary-color)">{CurrencyFormat(data.current_price)}</p>
                  </div>
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Tổng số bid</p>
                    <p className="mt-1 text-lg font-bold text-gray-800">{data.total_bids}</p>
                  </div>
                  <div className="rounded-xl bg-(--background-color) border border-gray-50 p-4">
                    <p className="text-[10px] uppercase tracking-widest text-gray-400 font-bold">Người tham gia</p>
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
                Xem trực tiếp
              </Link>
            ) : (
              <>
                <Link
                  href={`/seller-dashboard/auctions/${data.auction_id}/edit`}
                  className="flex-1 flex justify-center items-center gap-2 bg-(--primary-color) hover:bg-(--secondary-color) text-white font-bold py-3 rounded-xl transition-all active:scale-95"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /><path d="m15 5 4 4" /></svg>
                  Chỉnh sửa
                </Link>
                <Link
                  href={`/seller-dashboard/auctions/${data.auction_id}/delete`}
                  className="px-6 flex justify-center items-center bg-red-50 text-red-600 hover:bg-red-100 font-bold rounded-xl transition-all active:scale-95 border border-red-100"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18" /><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" /><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" /></svg>
                </Link>
              </>
            )}
          </div>
        </div>
    </div>
  );
};