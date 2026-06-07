"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AuctionEditForm } from "@/components/seller/auction/auction_edit_form";
import { NewAuction } from "@/types/auction/new-auction";
import { fetchAuction } from "@/services/fetch_auction";
import { updateAuction } from "@/services/update_auction";
import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";

export default function EditAuctionPage() {
    const params = useParams();
    const router = useRouter();

    const [auctionData, setAuctionData] = useState<NewAuction | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<unknown>(null);
    const [submitError, setSubmitError] = useState<string | null>(null);

    useEffect(() => {
        const loadAuction = async () => {
            if (!params.auction_id || Array.isArray(params.auction_id)) {
                setLoading(false);
                return;
            }

            try {
                setLoading(true);

                const data = await fetchAuction(params.auction_id);
                const newAuction: NewAuction = {
                    productId: data.product_id,
                    minimumBidIncrement: data.min_bid_increment,
                    startingPrice: data.starting_price,
                    startTime: data.start_time,
                    duration: data.duration,
                    depositAmount: data.deposit_amount
                }
                setAuctionData(newAuction);
            } catch (e) {
                setError(e);
            } finally {
                setLoading(false);
            }
        };

        loadAuction();
    }, [params.auction_id]);

    if (loading) {
        return <div className="p-10 text-center">Loading data...</div>;
    }

    if (error) {
        const status = getErrorStatus(error);
        return <ErrorView status={status} title={getErrorTitle(status)} message={getErrorMessage(error)} />;
    }

    if (!auctionData) {
        return <ErrorView status={404} title="Auction not found" />;
    }

    return (
        <div className="p-6 max-w-4xl mx-auto">

            {/* BACK BUTTON */}
            <button
                onClick={() => router.push("/seller-dashboard/auctions")}
                className="mb-6 text-sm text-gray-600 hover:text-(--primary-color) transition-colors"
            >
                Back to list
            </button>

            {submitError ? (
                <p className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{submitError}</p>
            ) : null}

            <AuctionEditForm
                initialData={auctionData}
                onSubmit={async (formData) => {
                    if (!params.auction_id || Array.isArray(params.auction_id)) {
                        return;
                    }

                    setSubmitError(null);
                    const newAuction: NewAuction = {
                        productId: formData.productId,
                        minimumBidIncrement: formData.minimumBidIncrement,
                        startingPrice: formData.startingPrice,
                        startTime: formData.startTime,
                        duration: formData.duration,
                        depositAmount: formData.depositAmount
                    };

                    try {
                        await updateAuction(params.auction_id, newAuction);
                        window.location.replace("/seller-dashboard/auctions/" + params.auction_id);
                    } catch (updateError) {
                        setSubmitError(getErrorMessage(updateError, "Failed to update auction."));
                    }
                }}
                isUpdating={true}
            />
        </div>
    );
}