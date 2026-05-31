"use client";

import { Auction } from "@/types/auction/auction";
import { ApprovalStatus, AuctionStatus } from "@/types/status";
import { DateFormat } from "@/utils/date_format";

interface AuctionItemProps {
    auction: Auction;
    onView: (id: string) => void;
    onEdit: (id: string) => void;
    onDelete: (id: string) => void;
}

const statusConfig: Record<AuctionStatus, { label: string, classes: string }> = {
    "NOT_STARTED": { label: 'Upcoming', classes: 'bg-amber-50 text-amber-600 border-amber-100' },
    "IN_PROGRESS": { label: 'Live', classes: 'bg-green-50 text-green-600 border-green-100' },
    "PAUSED": { label: 'Paused', classes: 'bg-yellow-50 text-yellow-600 border-yellow-100' },
    "ENDED": { label: 'Ended', classes: 'bg-gray-50 text-gray-500 border-gray-100' },
    "CANCELLED": { label: 'Cancelled', classes: 'bg-red-50 text-red-600 border-red-100' },
    "COMPLETED": { label: 'Completed', classes: 'bg-emerald-50 text-emerald-600 border-emerald-100' },
    "FAILED": { label: 'Failed', classes: 'bg-rose-50 text-rose-600 border-rose-100' },
};

const approvalStatusConfig: Record<ApprovalStatus, { label: string; classes: string }> = {
    PENDING: { label: 'Pending', classes: 'bg-amber-50 text-amber-700 border-amber-100' },
    APPROVED: { label: 'Approved', classes: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
    REJECTED: { label: 'Rejected', classes: 'bg-red-50 text-red-700 border-red-100' },
};

export const AuctionItem = ({ auction, onView, onEdit, onDelete }: AuctionItemProps) => {
    const status = statusConfig[auction.auction_status];

    return (
        <div className="bg-white p-4 rounded-2xl border border-gray-100 hover:border-(--accent-color) hover:shadow-sm transition-all flex items-center justify-between group">
            <div className="flex items-center gap-4">
                {/* Auction icon */}
                <div className="h-12 w-12 bg-(--background-color) rounded-xl flex items-center justify-center text-(--secondary-color)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                    </svg>
                </div>
                
                <div className="flex flex-col">
                    <div className="flex items-center gap-2">
                        <h3 className="font-bold text-gray-800 text-base">{auction.product_name}</h3>
                        {auction.approval_status === "APPROVED" && status && (
                            <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${status.classes}`}>
                                {status.label}
                            </span>
                        )}
                        <span className={`text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-lg border ${approvalStatusConfig[auction.approval_status]?.classes ?? "bg-gray-50 text-gray-500 border-gray-100"}`}>
                            {approvalStatusConfig[auction.approval_status]?.label ?? auction.approval_status ?? "Unknown"}
                        </span>
                    </div>
                    
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-1">
                        <span className="flex items-center gap-1 text-sm text-gray-500">
                            <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2" /><line x1="16" x2="16" y1="2" y2="6" /><line x1="8" x2="8" y1="2" y2="6" /><line x1="3" x2="21" y1="10" y2="10" /></svg>
                            {DateFormat(auction.start_time)}
                        </span>
                        <span className="flex items-center gap-1 text-sm font-medium text-(--secondary-color)">
                            <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" x2="12" y1="2" y2="22" /><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" /></svg>
                            {auction.starting_price.toLocaleString()} VND
                        </span>
                    </div>
                </div>
            </div>

            <div className="flex items-center gap-1">
                {/* View Action */}
                <button
                    onClick={() => onView(auction.auction_id)}
                    className="p-2 text-gray-400 hover:text-(--primary-color) hover:bg-blue-50 rounded-lg transition-colors"
                    title="View details"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></svg>
                </button>

                {/* Edit Action */}
                <button
                    onClick={() => onEdit(auction.auction_id)}
                    className="p-2 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                    title="Edit"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /><path d="m15 5 4 4" /></svg>
                </button>

                {/* Delete Action */}
                <button
                    onClick={() => onDelete(auction.auction_id)}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Delete"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18" /><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" /><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" /><line x1="10" x2="10" y1="11" y2="17" /><line x1="14" x2="14" y1="11" y2="17" /></svg>
                </button>
            </div>
        </div>
    );
};