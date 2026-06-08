"use client";

import { useEffect, useState } from "react";
import { Auction } from "@/types/auction/auction";
import { AuctionStatus } from "@/types/status";
import { fetchUserAuctionHistory } from "@/services/fetch_user_auction_history";
import { fetchAuctionDetails } from "@/services/fetch_user_auction_details";
import { AuctionRegistrationResponse } from "@/services/fetch_user_auction_history";

interface AuctionHistoryContentProps {
  userId: string;
}

interface AuctionHistoryItem {
  registration: AuctionRegistrationResponse;
  auction?: Auction;
  loading: boolean;
  error?: string;
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Failed to load auction history";
}

export function AuctionHistoryContent({ userId }: AuctionHistoryContentProps) {
  const [auctionHistory, setAuctionHistory] = useState<AuctionHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadAuctionHistory = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch user's auction registrations
        const registrations = await fetchUserAuctionHistory(userId);

        // For each registration, fetch the auction details
        const auctionItems: AuctionHistoryItem[] = registrations.map((reg) => ({
          registration: reg,
          loading: true,
        }));

        setAuctionHistory(auctionItems);

        // Fetch auction details for each registration
        for (let i = 0; i < registrations.length; i++) {
          try {
            const auction = await fetchAuctionDetails(registrations[i].auctionId);
            setAuctionHistory((prev) => {
              const updated = [...prev];
              updated[i] = {
                ...updated[i],
                auction,
                loading: false,
              };
              return updated;
            });
          } catch (err: unknown) {
            setAuctionHistory((prev) => {
              const updated = [...prev];
              updated[i] = {
                ...updated[i],
                loading: false,
                error: getErrorMessage(err),
              };
              return updated;
            });
          }
        }
      } catch (err: unknown) {
        console.error("Error fetching auction history:", err);
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      loadAuctionHistory();
    }
  }, [userId]);

  // Loading State
  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
        <div className="animate-pulse space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-20 bg-gray-200 rounded"></div>
          ))}
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
        <div className="flex items-center gap-2 text-red-600 bg-red-50 p-4 rounded-lg">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          <span>{error}</span>
        </div>
      </div>
    );
  }

  // Empty State
  if (auctionHistory.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-12 text-center">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-12 w-12 mx-auto text-gray-400 mb-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <p className="text-gray-500 text-lg">No auction history found</p>
        <p className="text-gray-400 text-sm mt-1">
          You haven&apos;t registered for any auctions yet
        </p>
      </div>
    );
  }

  // Helper function for status badge
  const getStatusBadge = (status: AuctionStatus | string) => {
    const normalized = status?.toUpperCase?.() || "";

    if (normalized === "IN_PROGRESS") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
          <span className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
          In Progress
        </span>
      );
    }
    if (normalized === "NOT_STARTED") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          <span className="w-1.5 h-1.5 bg-blue-500 rounded-full"></span>
          Not Started
        </span>
      );
    }
    if (normalized === "PAUSED") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          <span className="w-1.5 h-1.5 bg-yellow-500 rounded-full"></span>
          Paused
        </span>
      );
    }
    if (normalized === "ENDED" || normalized === "COMPLETED") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          <span className="w-1.5 h-1.5 bg-gray-500 rounded-full"></span>
          {normalized === "COMPLETED" ? "Completed" : "Ended"}
        </span>
      );
    }
    if (normalized === "CANCELLED" || normalized === "FAILED") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
          <span className="w-1.5 h-1.5 bg-red-500 rounded-full"></span>
          {status}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
        {status}
      </span>
    );
  };

  // Main Table View
  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-100">
        <h2 className="text-lg font-semibold text-[var(--secondary-color)]">
          Auction History
        </h2>
        <p className="text-sm text-gray-500 mt-1">
          Your registered and participated auctions
        </p>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                Product
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                Starting Price
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                Current Price
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                Registered Date
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {auctionHistory.map((item, idx) => (
              <tr
                key={idx}
                className="hover:bg-gray-50 transition-colors"
              >
                <td className="px-6 py-4">
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {item.loading ? (
                        <span className="inline-block w-32 h-4 bg-gray-200 rounded animate-pulse"></span>
                      ) : item.error ? (
                        <span className="text-red-600">Error loading</span>
                      ) : (
                        item.auction?.product_name || "Unknown"
                      )}
                    </p>
                    <p className="text-xs text-gray-500 mt-1">
                      {item.loading ? (
                        <span className="inline-block w-24 h-3 bg-gray-200 rounded animate-pulse"></span>
                      ) : (
                        `ID: ${item.registration.auctionId.slice(0, 8)}...`
                      )}
                    </p>
                  </div>
                </td>
                <td className="px-6 py-4">
                  {item.loading ? (
                    <span className="inline-block w-20 h-6 bg-gray-200 rounded animate-pulse"></span>
                  ) : item.error ? (
                    <span className="text-red-600 text-xs">Error</span>
                  ) : (
                    getStatusBadge(item.auction?.auction_status || "unknown")
                  )}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {item.loading ? (
                    <span className="inline-block w-24 h-4 bg-gray-200 rounded animate-pulse"></span>
                  ) : item.error ? (
                    <span className="text-red-600">—</span>
                  ) : (
                    `${item.auction?.starting_price?.toLocaleString() || "—"} VND`
                  )}
                </td>
                <td className="px-6 py-4 text-sm font-medium text-gray-900">
                  {item.loading ? (
                    <span className="inline-block w-24 h-4 bg-gray-200 rounded animate-pulse"></span>
                  ) : item.error ? (
                    <span className="text-red-600">—</span>
                  ) : (
                    `${item.auction?.current_price?.toLocaleString() || "—"} VND`
                  )}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {new Date(item.registration.registrationTime).toLocaleDateString("vi-VN")}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
