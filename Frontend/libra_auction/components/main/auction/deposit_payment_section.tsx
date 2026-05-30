'use client';

import { useState, useEffect } from "react";
import { Auction } from "@/types/auction/auction";
import { CurrencyFormat } from "@/utils/currency_format";
import { createDeposit, isDepositPaid } from "@/services/create_deposit";
import Button from "@/components/ui/Button";

interface DepositPaymentSectionProps {
    auction: Auction;
    paymentStatus: string | null;
}

export default function DepositPaymentSection({
    auction,
    paymentStatus,
}: DepositPaymentSectionProps) {
    const [isProcessing, setIsProcessing] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [depositPaid, setDepositPaid] = useState(paymentStatus === "success");

    useEffect(() => {
        const checkDeposit = async () => {
            try {
                const paid = await isDepositPaid(auction.auction_id);
                if (paid) {
                    setDepositPaid(true);
                }
            } catch {
                // Ignore errors
            }
        };
        checkDeposit();
    }, [auction.auction_id]);

    const handlePayDeposit = async () => {
        setIsProcessing(true);
        setError(null);
        try {
            const paymentUrl = await createDeposit(auction.auction_id);
            window.location.href = paymentUrl;
        } catch (err) {
            setError(err instanceof Error ? err.message : "Không thể tạo thanh toán");
            setIsProcessing(false);
        }
    };

    // Payment success
    if (depositPaid) {
        return (
            <div className="bg-green-50 border border-green-200 rounded-lg p-6 text-center">
                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                </div>
                <h3 className="text-lg font-bold text-green-800 mb-2">
                    Đăng ký hoàn tất!
                </h3>
                <p className="text-green-600 mb-4">
                    Bạn đã thanh toán tiền cọc thành công. Bạn có thể tham gia đấu giá khi phiên bắt đầu.
                </p>
                <a
                    href={`/auctions/${auction.category_id}/${auction.auction_id}/live`}
                    className="inline-block"
                >
                    <Button variant="primary">
                        Xem đấu giá trực tiếp
                    </Button>
                </a>
            </div>
        );
    }

    // Payment failed
    if (paymentStatus === "failed") {
        return (
            <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </div>
                <h3 className="text-lg font-bold text-red-800 mb-2">
                    Thanh toán thất bại
                </h3>
                <p className="text-red-600 mb-4">
                    Giao dịch không thành công. Vui lòng thử lại.
                </p>
                {error && (
                    <div className="bg-red-100 text-red-700 text-sm p-3 rounded-lg mb-4">
                        {error}
                    </div>
                )}
                <Button
                    variant="primary"
                    onClick={handlePayDeposit}
                    loading={isProcessing}
                >
                    Thử lại
                </Button>
            </div>
        );
    }

    // Default: pay deposit
    return (
        <div className="bg-white border border-gray-200 rounded-lg p-6">
            <h3 className="text-lg font-bold text-gray-800 mb-4">
                Thanh toán tiền cọc
            </h3>
            <div className="bg-gray-50 rounded-lg p-4 mb-4">
                <div className="flex justify-between items-center">
                    <span className="text-gray-500">Số tiền cọc:</span>
                    <span className="text-2xl font-bold text-[#146C94]">
                        {CurrencyFormat(auction.deposit_amount)}
                    </span>
                </div>
            </div>
            <p className="text-sm text-gray-500 mb-4">
                Bạn cần thanh toán tiền cọc qua VNPay để hoàn tất đăng ký tham gia đấu giá.
                Tiền cọc sẽ được hoàn trả nếu bạn không trúng đấu giá.
            </p>
            {error && (
                <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg mb-4">
                    {error}
                </div>
            )}
            <Button
                variant="primary"
                size="lg"
                className="w-full"
                onClick={handlePayDeposit}
                loading={isProcessing}
            >
                Thanh toán cọc qua VNPay
            </Button>
        </div>
    );
}
