'use client';
import { Auction } from "@/types/auction/auction";
import { ApprovalStatus } from "@/types/status";
import Image from "next/image";
import { useRouter } from "next/navigation";

const approvalStatusConfig: Record<ApprovalStatus, { label: string; classes: string }> = {
    CHUA_DUYET: { label: "Pending", classes: "bg-amber-500/90 text-white" },
    DA_DUYET: { label: "Approved", classes: "bg-emerald-500/90 text-white" },
    BI_TU_CHOI: { label: "Rejected", classes: "bg-red-500/90 text-white" },
};

export default function AuctionCard({ auctionCard }: { auctionCard: Auction }) {
    const router = useRouter();
    // Hàm format tiền tệ
    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0
        }).format(amount);
    };

    return (
        // Bỏ scale, thêm hover:border-[#19A7CE] và hover:shadow-[...] để tạo hiệu ứng viền phát sáng
        <div className="group bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:border-[#19A7CE] hover:shadow-[0_0_15px_rgba(25,167,206,0.3)] transition-all duration-300 flex flex-col h-full">
            {/* Image Wrapper */}
            <div className="relative aspect-square overflow-hidden bg-gray-100">
                <Image
                    src={auctionCard.images[0] || "/placeholder.jpg"}
                    alt={auctionCard.product_name}
                    fill
                    className="object-cover"
                />

                {/* Overlay Badge - Loại đấu giá */}
                <div className="absolute top-3 left-3 right-3 flex items-start justify-between gap-2">
                    <span className="px-2.5 py-1 bg-black/70 text-white text-[10px] font-bold uppercase rounded-lg">
                        {auctionCard.category_name}
                    </span>
                    <span className={`px-2.5 py-1 text-[10px] font-bold uppercase rounded-lg ${approvalStatusConfig[auctionCard.approval_status].classes}`}>
                        {approvalStatusConfig[auctionCard.approval_status].label}
                    </span>
                </div>

                {/* Thời gian còn lại - Tối giản */}
                <div className="absolute bottom-3 left-3 right-3 bg-white/95 py-2 px-3 rounded-xl shadow flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <span className="rounded-full h-2 w-2 bg-red-500"></span>
                        <span className="text-[11px] font-bold text-gray-700 uppercase tracking-tight">Kết thúc sau</span>
                    </div>
                    <span className="text-xs font-mono font-bold text-red-600">02:45:10</span>
                </div>
            </div>

            {/* Content Area */}
            <div className="p-4 flex flex-col grow">
                {/* Tên sản phẩm */}
                <h3 className="font-bold text-gray-800 line-clamp-2 leading-tight mb-3">
                    {auctionCard.product_name}
                </h3>

                {/* Section: Pricing & Stats */}
                <div className="mt-auto pt-3 border-t border-gray-50">
                    {/* Dòng giá chính */}
                    <div className="flex justify-between items-end mb-3">
                        <div className="flex flex-col">
                            <span className="text-[10px] font-bold text-gray-400 uppercase leading-none mb-1">Giá hiện tại</span>
                            <span className="text-xl font-black text-[#19A7CE] leading-none tracking-tight">
                                {formatCurrency(auctionCard.current_price)}
                            </span>
                        </div>
                        <div className="text-right">
                            <span className="block text-[9px] text-gray-400 uppercase leading-none mb-1">Khởi điểm</span>
                            <span className="text-xs font-bold text-gray-600 leading-none">
                                {formatCurrency(auctionCard.starting_price)}
                            </span>
                        </div>
                    </div>

                    {/* Dòng thông số & Nút */}
                    <div className="flex items-center gap-3">
                        {/* Stats */}
                        <div className="flex items-center gap-3 shrink-0 bg-gray-50 px-3 py-2 rounded-lg">
                            <div className="flex items-center gap-1">
                                <svg className="w-3 h-3 text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h4l4 4 4-4h4c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" /></svg>
                                <span className="text-xs font-bold text-gray-700">{auctionCard.total_bids}</span>
                            </div>
                            <div className="w-px h-3 bg-gray-200"></div>
                            <div className="flex items-center gap-1">
                                <svg className="w-3 h-3 text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3z" /></svg>
                                <span className="text-xs font-bold text-gray-700">{auctionCard.total_participants}</span>
                            </div>
                        </div>

                        {/* Nút bấm */}
                        <button className="grow bg-[#19A7CE] hover:opacity-90 text-white text-xs font-bold py-2 rounded-lg transition-opacity"
                        onClick={() => router.push(`/auctions/${auctionCard.category_id}/${auctionCard.auction_id}`)}>
                            Tham gia ngay
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}