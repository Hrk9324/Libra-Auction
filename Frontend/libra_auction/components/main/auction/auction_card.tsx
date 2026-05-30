'use client';
import { Auction } from "@/types/auction/auction";
import { AuctionStatus } from "@/types/status";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const auctionStatusConfig: Record<AuctionStatus, { label: string; classes: string }> = {
    NOT_STARTED: { label: "Upcoming", classes: "bg-blue-600/90 text-white" },
    IN_PROGRESS: { label: "Live", classes: "bg-emerald-500/90 text-white" },
    PAUSED: { label: "Paused", classes: "bg-yellow-500/90 text-white" },
    ENDED: { label: "Ended", classes: "bg-gray-600/90 text-white" },
    CANCELLED: { label: "Cancelled", classes: "bg-red-500/90 text-white" },
};

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

    const formatCountdown = (targetTime: Date, currentTime: number) => {
        const remainingMs = targetTime.getTime() - currentTime;
        const safeRemainingMs = Math.max(0, remainingMs);
        const totalSeconds = Math.floor(safeRemainingMs / 1000);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        return [hours, minutes, seconds]
            .map((value) => String(value).padStart(2, "0"))
            .join(":");
    };

    const auctionEndTime = new Date(new Date(auctionCard.start_time).getTime() + auctionCard.duration * 1000);
    const countdownLabel = isLive ? "Ends in" : "Starts in";
    const countdownValue = isLive
        ? formatCountdown(auctionEndTime, now)
        : formatCountdown(new Date(auctionCard.start_time), now);
    const primaryStatLabel = isLive ? "Current price" : "Starting price";
    const primaryStatValue = isLive ? auctionCard.current_price : auctionCard.starting_price;
    const secondaryStatLabel = isLive ? "Starting price" : "Participants";
    const secondaryStatValue = isLive ? auctionCard.starting_price : auctionCard.total_participants;
    const secondaryStatDisplay = isLive
        ? formatCurrency(secondaryStatValue)
        : secondaryStatValue.toLocaleString("vi-VN");
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
                        <span className="rounded-full h-2 w-2 bg-red-500"></span>
                        <span className="text-[11px] font-bold text-gray-700 uppercase tracking-tight">{countdownLabel}</span>
                    </div>
                    <span className="text-xs font-mono font-bold text-red-600">{countdownValue}</span>
                </div>
            </div>

            <div className="p-4 flex flex-col grow">
                <h3 className="font-bold text-gray-800 line-clamp-2 leading-tight mb-3">
                    {auctionCard.product_name}
                </h3>

                <div className="mt-auto pt-3 border-t border-gray-50">
                    <div className="flex justify-between items-end mb-3">
                        <div className="flex flex-col">
                            <span className="text-[10px] font-bold text-gray-400 uppercase leading-none mb-1">{primaryStatLabel}</span>
                            <span className="text-xl font-black text-[#19A7CE] leading-none tracking-tight">
                                {formatCurrency(primaryStatValue)}
                            </span>
                        </div>
                        <div className="text-right">
                            <span className="block text-[9px] text-gray-400 uppercase leading-none mb-1">{secondaryStatLabel}</span>
                            <span className="text-xs font-bold text-gray-600 leading-none">
                                {secondaryStatDisplay}
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-3">
                        <div className="flex items-center gap-3 shrink-0 bg-gray-50 px-3 py-2 rounded-lg">
                            <div className="flex items-center gap-1">
                                <svg className="w-3 h-3 text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h4l4 4 4-4h4c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" /></svg>
                                <span className="text-xs font-bold text-gray-700">{secondaryStatValue}</span>
                            </div>
                            <div className="w-px h-3 bg-gray-200"></div>
                            <div className="flex items-center gap-1">
                                <svg className="w-3 h-3 text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3z" /></svg>
                                <span className="text-xs font-bold text-gray-700">{auctionCard.total_participants}</span>
                            </div>
                        </div>

                        <button className="grow bg-[#19A7CE] hover:opacity-90 text-white text-xs font-bold py-2 rounded-lg transition-opacity"
                        onClick={() => router.push(`/auctions/${auctionCard.category_id}/${auctionCard.auction_id}`)}>
                            {actionLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}