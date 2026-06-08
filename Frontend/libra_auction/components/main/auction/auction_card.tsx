'use client';
import { Auction } from "@/types/auction/auction";
import { AuctionStatus } from "@/types/status";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const auctionStatusConfig: Record<AuctionStatus, { label: string; classes: string }> = {
    NOT_STARTED: { label: "Upcoming", classes: "bg-emerald-500/90 text-white" },
    IN_PROGRESS: { label: "Live", classes: "bg-red-500/90 text-white" },
    PAUSED: { label: "Paused", classes: "bg-yellow-500/90 text-white" },
    ENDED: { label: "Ended", classes: "bg-gray-600/90 text-white" },
    CANCELLED: { label: "Cancelled", classes: "bg-red-500/90 text-white" },
    COMPLETED: { label: "Completed", classes: "bg-emerald-600/90 text-white" },
    FAILED: { label: "Failed", classes: "bg-rose-500/90 text-white" },
};

function normalizeSpringIsoDate(value: string) {
    return value.replace(/\.(\d{3})\d+(Z|[+-]\d{2}:?\d{2})$/, ".$1$2");
}

function toTimestamp(value: Date | string | number | undefined) {
    if (value instanceof Date) return value.getTime();
    if (typeof value === "number") return value;
    if (typeof value === "string") return new Date(normalizeSpringIsoDate(value)).getTime();
    return NaN;
}

export default function AuctionCard({ auctionCard }: { auctionCard: Auction }) {
    const router = useRouter();
    const [now, setNow] = useState(() => Date.now());

    const isLive = auctionCard.auction_status === "IN_PROGRESS";
    const status = auctionStatusConfig[auctionCard.auction_status] ?? { label: 'Unknown', classes: 'bg-gray-50 text-gray-500 border-gray-100' };

    useEffect(() => {
        const timerId = window.setInterval(() => {
            setNow(Date.now());
        }, 1000);

        return () => window.clearInterval(timerId);
    }, []);

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0
        }).format(amount);
    };

    const formatCountdown = (targetTime: number, currentTime: number) => {
        const remainingMs = targetTime - currentTime;
        const safeRemainingMs = Math.max(0, remainingMs);
        const totalSeconds = Math.floor(safeRemainingMs / 1000);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        return [hours, minutes, seconds]
            .map((value) => String(value).padStart(2, "0"))
            .join(":");
    };

    const explicitEndTime = toTimestamp(auctionCard.end_time);
    const startTime = toTimestamp(auctionCard.start_time);
    const auctionEndTime = Number.isNaN(explicitEndTime)
        ? startTime + auctionCard.duration * 1000
        : explicitEndTime;
    const countdownLabel = isLive ? "Ends in" : "Starts in";
    const countdownValue = isLive
        ? formatCountdown(auctionEndTime, now)
        : formatCountdown(startTime, now);
    const primaryStatLabel = isLive ? "Current price" : "Starting price";
    const primaryStatValue = isLive
        ? formatCurrency(auctionCard.current_price)
        : formatCurrency(auctionCard.starting_price);
    const secondaryStatLabel = "Participants";
    const secondaryStatValue = auctionCard.total_participants.toLocaleString("vi-VN");
    const bidStatValue = auctionCard.total_bids.toLocaleString("vi-VN");
    const actionLabel = isLive ? "Join now" : "View details";

    return (
        <div className="group bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:border-[#19A7CE] hover:shadow-[0_0_15px_rgba(25,167,206,0.3)] transition-all duration-300 flex flex-col h-full">
            <div className="relative aspect-square overflow-hidden bg-gray-100">
                <Image
                    src={auctionCard.images[0] || "/placeholder.jpg"}
                    alt={auctionCard.product_name}
                    fill
                    className="object-cover"
                />

                <div className="absolute top-3 left-3 right-3 flex items-start justify-between gap-2">
                    <span className="px-2.5 py-1 bg-black/70 text-white text-[10px] font-bold uppercase rounded-lg">
                        {auctionCard.category_name}
                    </span>
                    <span className={`px-2.5 py-1 text-[10px] font-bold uppercase rounded-lg ${status.classes}`}>
                        {status.label}
                    </span>
                </div>

                <div className="absolute bottom-3 left-3 right-3 bg-white/95 py-2 px-3 rounded-xl shadow flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <span className={`rounded-full h-2 w-2 ${isLive ? "bg-red-500" : "bg-emerald-500"}`}></span>
                        <span className="text-[11px] font-bold text-gray-700 uppercase tracking-tight">{countdownLabel}</span>
                    </div>
                    <span className={`text-xs font-mono font-bold ${isLive ? "text-red-600" : "text-emerald-600"}`}>{countdownValue}</span>
                </div>
            </div>

            <div className="p-4 flex flex-col grow">
                {/* Thay đổi từ line-clamp-2 thành truncate block để ép về 1 dòng và thêm tooltip title khi hover */}
                <h3 
                    className="font-bold text-gray-800 truncate block leading-tight mb-3"
                    title={auctionCard.product_name}
                >
                    {auctionCard.product_name}
                </h3>

                <div className="mt-auto pt-3 border-t border-gray-50">
                    <div className="flex justify-between items-end mb-4">
                        <div className="flex flex-col">
                            <span className="text-[10px] font-bold text-gray-400 uppercase leading-none mb-1.5">{primaryStatLabel}</span>
                            <span className="text-xl font-black text-[#19A7CE] leading-none tracking-tight">
                                {primaryStatValue}
                            </span>
                        </div>
                        <div className="min-w-16 text-right">
                            <span className="block text-[9px] text-gray-400 uppercase leading-none mb-1.5">{secondaryStatLabel}</span>
                            <span className="text-xs font-bold text-gray-600 leading-none">
                                {secondaryStatValue}
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        <div className="flex items-center justify-center gap-2 shrink-0 rounded-xl bg-gray-50 px-3.5 py-2.5 border border-gray-100 min-w-14 h-[38px]">
                            <svg className="h-3.5 w-3.5 text-gray-800 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M3 21h18M14 4h6m0 0v6m0-6L10 14M4 14.243a3 3 0 114.243-4.243L16.5 18l-4.243 4.243-8.257-8M16.5 18l4.243-4.243" />
                            </svg>

                            <span
                                className="text-xs font-extrabold text-gray-800 truncate max-w-[45px] block leading-none"
                                title={bidStatValue}
                            >
                                {bidStatValue}
                            </span>
                        </div>

                        <button className="flex-1 bg-[#19A7CE] hover:bg-[#146C94] text-white text-xs font-bold py-2.5 h-[38px] rounded-xl shadow-sm shadow-[#19A7CE]/20 transition-all duration-200 active:scale-[0.98]"
                            onClick={() => router.push(`/auctions/${auctionCard.category_id}/${auctionCard.auction_id}`)}>
                            {actionLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}