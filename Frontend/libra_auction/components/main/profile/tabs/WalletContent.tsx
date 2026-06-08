"use client";

import { useEffect, useState } from "react";
import {
  UserTransactionResponse,
  fetchTransactionHistory,
} from "@/services/fetch_transaction_history";

interface WalletContentProps {
  userId: string;
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Failed to load wallet data";
}

export function WalletContent({ userId }: WalletContentProps) {
  const [transactions, setTransactions] = useState<UserTransactionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadWalletData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch wallet balance and transaction history
        const [transactionList] = await Promise.all([
          fetchTransactionHistory(userId),
        ]);

        setTransactions(transactionList);
      } catch (err: unknown) {
        console.error("Error fetching wallet data:", err);
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      loadWalletData();
    }
  }, [userId]);

  // Loading State
  if (loading) {
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
          <div className="animate-pulse h-32 bg-gray-200 rounded"></div>
        </div>
        <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
          <div className="animate-pulse space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 rounded"></div>
            ))}
          </div>
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

  // Helper function for status badge
  const getStatusBadge = (status: string) => {
    const statusLower = status?.toLowerCase();
    if (statusLower === "success") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
          <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
          Success
        </span>
      );
    }
    if (statusLower === "processing") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          <span className="w-1.5 h-1.5 bg-yellow-500 rounded-full animate-pulse"></span>
          Processing
        </span>
      );
    }
    if (statusLower === "refunded") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          <span className="w-1.5 h-1.5 bg-blue-500 rounded-full"></span>
          Refunded
        </span>
      );
    }
    if (statusLower === "cancelled") {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          <span className="w-1.5 h-1.5 bg-gray-500 rounded-full"></span>
          Cancelled
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
        <span className="w-1.5 h-1.5 bg-red-500 rounded-full"></span>
        Failed
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Transaction History */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-100">
          <h2 className="text-lg font-semibold text-[var(--secondary-color)]">
            Transaction History
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Your recent wallet transactions
          </p>
        </div>

        {/* Empty State */}
        {transactions.length === 0 ? (
          <div className="p-12 text-center">
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
                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="text-gray-500 text-lg">No transactions yet</p>
            <p className="text-gray-400 text-sm mt-1">
              Your transaction history will appear here
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                    Description
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">
                    Reference
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {transactions.map((txn, idx) => (
                  <tr key={idx} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4">
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {txn.description}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {txn.detailInfo}
                        </p>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">
                      {txn.incoming ? "+" : "-"}
                      {txn.amount.toLocaleString("vi-VN")} VND
                    </td>
                    <td className="px-6 py-4">
                      {getStatusBadge(txn.status)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {txn.createdAt}
                    </td>
                    <td className="px-6 py-4 text-xs text-gray-500 font-mono">
                      {(txn.vnpayTxnRef || txn.transactionId).slice(0, 12)}...
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
