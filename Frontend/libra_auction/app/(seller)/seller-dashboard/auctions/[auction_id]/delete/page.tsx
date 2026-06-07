"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";
import { AuctionDeleteConfirm } from "@/components/seller/auction/auction_delete_confirm";
import { Auction } from "@/types/auction/auction";
import { fetchAuction } from "@/services/fetch_auction";
import { deleteAuction } from "@/services/delete_auction";

export default function DeleteAuctionPage() {
  const params = useParams();
  const router = useRouter();

  const auctionId = params.auction_id as string;

  const [auction, setAuction] = useState<Auction | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  // =========================
  // FETCH DETAIL AUCTION
  // =========================
  useEffect(() => {
    const loadAuction = async () => {
      if (!auctionId) return;

      try {
        setLoading(true);
        if (!params.auction_id || Array.isArray(params.auction_id)) {
          return;
        }

        const data = await fetchAuction(params.auction_id);

        setAuction(data);
      } catch (error) {
        setError(error);
      } finally {
        setLoading(false);
      }
    };

    loadAuction();
  }, [auctionId, router, params.auction_id]);

  // =========================
  // DELETE AUCTION
  // =========================
  const handleDelete = async () => {
    try {
      if (!params.auction_id || Array.isArray(params.auction_id)) {
        return;
      }

      await deleteAuction(params.auction_id);

      router.push("/seller-dashboard/auctions");
      router.refresh();
    } catch (error) {
      setDeleteError(getErrorMessage(error, "An error occurred while deleting the auction."));
    }
  };

  // =========================
  // UI STATES
  // =========================
  if (loading) {
    return (
      <div className="p-10 text-center italic text-gray-400">
        Checking data...
      </div>
    );
  }

  if (error) {
    const status = getErrorStatus(error);
    return <ErrorView status={status} title={getErrorTitle(status)} message={getErrorMessage(error)} />;
  }

  if (!auction) {
    return <ErrorView status={404} title="Auction not found" />;
  }

  // =========================
  // RENDER
  // =========================
  return (
    <div className="min-h-screen bg-(--background-color) flex items-center justify-center p-6">
      <div className="w-full max-w-2xl space-y-4">
        {deleteError ? (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{deleteError}</p>
        ) : null}
        <AuctionDeleteConfirm
          auction={auction}
          onDelete={handleDelete}
          onCancel={() => router.back()}
        />
      </div>
    </div>
  );
}