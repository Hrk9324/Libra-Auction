'use client';

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Auction } from "@/types/auction/auction";
import { UserInfo } from "@/types/user_info";
import { CurrencyFormat } from "@/utils/currency_format";
import { registerForAuction } from "@/services/register_auction";
import Button from "@/components/ui/Button";

interface RegistrationConfirmDialogProps {
    isOpen: boolean;
    onClose: () => void;
    auction: Auction;
    userInfo: UserInfo;
    categoryId: string;
}

export default function RegistrationConfirmDialog({
    isOpen,
    onClose,
    auction,
    userInfo,
    categoryId,
}: RegistrationConfirmDialogProps) {
    const router = useRouter();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    if (!isOpen) return null;

    const handleConfirm = async () => {
        setIsSubmitting(true);
        setError(null);
        try {
            await registerForAuction(auction.auction_id);
            onClose();
            router.push(`/auctions/${categoryId}/${auction.auction_id}/registration`);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Đăng ký thất bại");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <>
            {/* Overlay */}
            <div
                className="fixed inset-0 bg-black/40 bg-opacity-50 z-50 transition-opacity"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className="fixed inset-0 flex items-center justify-center z-50 p-6">
                <div
                    className="bg-white rounded-xl shadow-2xl w-full max-w-2xl"
                    onClick={(e) => e.stopPropagation()}
                >
                    {/* Header */}
                    <div className="flex items-center justify-between px-8 py-5 border-b border-gray-200">
                        <h3 className="text-2xl font-bold text-[#146C94]">
                            Xác nhận đăng ký đấu giá
                        </h3>
                        <button
                            onClick={onClose}
                            className="text-gray-500 hover:text-gray-700 transition text-3xl leading-none"
                        >
                            ×
                        </button>
                    </div>

                    {/* Body */}
                    <div className="px-8 py-6 space-y-5">
                        <div className="bg-gray-50 rounded-xl px-6 py-5 space-y-4">
                            <div className="flex justify-between items-center">
                                <span className="text-gray-500 min-w-[140px]">Phiên đấu giá:</span>
                                <span className="font-medium text-gray-800 text-right flex-1 ml-4">
                                    {auction.auction_name}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-500 min-w-[140px]">Sản phẩm:</span>
                                <span className="font-medium text-gray-800 text-right flex-1 ml-4">
                                    {auction.product_name}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-500 min-w-[140px]">Người đăng ký:</span>
                                <span className="font-medium text-gray-800 text-right flex-1 ml-4">
                                    {userInfo.fullName}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-gray-500 min-w-[140px]">Email:</span>
                                <span className="font-medium text-gray-800 text-right flex-1 ml-4">
                                    {userInfo.email}
                                </span>
                            </div>
                            <div className="border-t border-gray-200 pt-4 flex justify-between items-center">
                                <span className="text-gray-500 min-w-[140px]">Tiền cọc:</span>
                                <span className="font-bold text-[#146C94] text-xl">
                                    {CurrencyFormat(auction.deposit_amount)}
                                </span>
                            </div>
                        </div>

                        <p className="text-base text-gray-500 leading-relaxed">
                            Sau khi đăng ký, bạn cần thanh toán tiền cọc qua VNPay để hoàn tất
                            quá trình đăng ký tham gia đấu giá.
                        </p>

                        {error && (
                            <div className="bg-red-50 text-red-600 text-sm p-4 rounded-lg">
                                {error}
                            </div>
                        )}
                    </div>

                    {/* Footer */}
                    <div className="flex items-center justify-end gap-4 px-8 py-5 border-t border-gray-200 bg-gray-50 rounded-b-xl">
                        <button
                            onClick={onClose}
                            disabled={isSubmitting}
                            className="px-6 py-3 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-100 transition font-medium disabled:opacity-50"
                        >
                            Hủy
                        </button>
                        <button
                            onClick={handleConfirm}
                            disabled={isSubmitting}
                            className="px-6 py-3 bg-[#146C94] text-white rounded-lg hover:bg-[#0f5474] transition font-medium disabled:opacity-50 flex items-center gap-2"
                        >
                            {isSubmitting && (
                                <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                                </svg>
                            )}
                            Xác nhận đăng ký
                        </button>
                    </div>
                </div>
            </div>
        </>
    );
}
