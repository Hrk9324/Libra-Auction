'use client';

import { useState, useEffect } from "react";
import Image from "next/image";
import AuctionDetailModal from "@/components/admin/auction_detail_modal";

interface AuctionData {
  id: string;
  image: string;
  name: string;
  seller: string;
  startingPrice: number;
  createdAt: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  description: string;
  startTime: string;
  endTime: string;
  images: string[];
  category: string;
}

// Mock Auctions Data
const mockAuctions: AuctionData[] = [
  {
    id: "1",
    image: "/default-avatar.png",
    name: "Laptop Dell XPS 13",
    seller: "Nguyễn Văn A",
    startingPrice: 15000000,
    createdAt: "2026-05-10",
    status: "PENDING",
    description:
      "High-end laptop with Intel i7, 16GB RAM, 512GB SSD. Excellent condition, minimal use.",
    startTime: "2026-05-20 10:00 AM",
    endTime: "2026-05-27 10:00 AM",
    images: ["/default-avatar.png", "/default-avatar.png", "/default-avatar.png"],
    category: "Electronics",
  },
  {
    id: "2",
    image: "/default-avatar.png",
    name: "Vintage Leather Sofa",
    seller: "Trần Thị B",
    startingPrice: 8000000,
    createdAt: "2026-05-11",
    status: "PENDING",
    description:
      "Beautiful vintage leather sofa, handcrafted in Italy. Perfect for collectors.",
    startTime: "2026-05-21 02:00 PM",
    endTime: "2026-05-28 02:00 PM",
    images: ["/default-avatar.png", "/default-avatar.png"],
    category: "Furniture",
  },
  {
    id: "3",
    image: "/default-avatar.png",
    name: "Gold Ring with Diamond",
    seller: "Lê Minh C",
    startingPrice: 25000000,
    createdAt: "2026-05-09",
    status: "APPROVED",
    description: "18K gold ring with 2-carat diamond, certified authentic.",
    startTime: "2026-05-18 06:00 PM",
    endTime: "2026-05-25 06:00 PM",
    images: ["/default-avatar.png", "/default-avatar.png", "/default-avatar.png", "/default-avatar.png"],
    category: "Jewelry",
  },
  {
    id: "4",
    image: "/default-avatar.png",
    name: "Vintage Rolex Watch",
    seller: "Phạm Văn D",
    startingPrice: 30000000,
    createdAt: "2026-05-08",
    status: "REJECTED",
    description:
      "Classic Rolex Submariner from 1970s, needs restoration. Collector's item.",
    startTime: "2026-05-22 11:00 AM",
    endTime: "2026-05-29 11:00 AM",
    images: ["/default-avatar.png", "/default-avatar.png"],
    category: "Watches",
  },
];

export default function AuctionsApprovalPage() {
  // Store list of auctions to be reviewed
  const [auctions, setAuctions] = useState<AuctionData[]>(mockAuctions);
  // Store selected auction for detail modal
  const [selectedAuction, setSelectedAuction] = useState<{
    isOpen: boolean;
    data: AuctionData | null;
  }>({ isOpen: false, data: null });
  // Store selected status filter
  const [statusFilter, setStatusFilter] = useState<"ALL" | "PENDING" | "APPROVED" | "REJECTED">("ALL");

  // Fetch pending auctions from backend on component mount
  useEffect(() => {
    const fetchPendingAuctions = async () => {
      try {
        const response = await fetch('/api/admin/auctions/pending');
        if (response.ok) {
          const data = await response.json();
          setAuctions(data);
        } else {
          console.error('Failed to fetch pending auctions:', response.statusText);
        }
      } catch (error) {
        console.error('Error fetching pending auctions:', error);
      }
    };
    fetchPendingAuctions();
  }, []);

  // Send approval request to backend and update local state
  const handleApprove = async (auction: AuctionData) => {
    try {
      const response = await fetch(`/api/admin/auctions/${auction.id}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.ok) {
        // Update auction status to APPROVED in UI
        setAuctions(
          auctions.map((a) =>
            a.id === auction.id ? { ...a, status: "APPROVED" } : a
          )
        );
        console.log("Auction approved:", auction.id);
      } else {
        console.error('Failed to approve auction:', response.statusText);
      }
    } catch (error) {
      console.error('Error approving auction:', error);
    }
  };

  // Send rejection request to backend and update local state
  const handleReject = async (auction: AuctionData) => {
    try {
      const response = await fetch(`/api/admin/auctions/${auction.id}/reject`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.ok) {
        // Update auction status to REJECTED in UI
        setAuctions(
          auctions.map((a) =>
            a.id === auction.id ? { ...a, status: "REJECTED" } : a
          )
        );
        console.log("Auction rejected:", auction.id);
      } else {
        console.error('Failed to reject auction:', response.statusText);
      }
    } catch (error) {
      console.error('Error rejecting auction:', error);
    }
  };

  const handleViewDetails = (auction: AuctionData) => {
    setSelectedAuction({ isOpen: true, data: auction });
  };

  const getStatusBadge = (status: string) => {
    const statusStyles = {
      PENDING: "bg-amber-100 text-amber-800 border border-amber-300",
      APPROVED: "bg-green-100 text-green-800 border border-green-300",
      REJECTED: "bg-red-100 text-red-800 border border-red-300",
    };
    const labels = {
      PENDING: "Pending",
      APPROVED: "Approved",
      REJECTED: "Rejected",
    };
    return (
      <span
        className={`px-3 py-1 rounded-full text-xs font-semibold ${
          statusStyles[status as keyof typeof statusStyles]
        }`}
      >
        {labels[status as keyof typeof labels]}
      </span>
    );
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price);
  };

  const pendingCount = auctions.filter((a) => a.status === "PENDING").length;
  const approvedCount = auctions.filter((a) => a.status === "APPROVED").length;
  const rejectedCount = auctions.filter((a) => a.status === "REJECTED").length;

  // Filter auctions based on selected status
  const filteredAuctions = statusFilter === "ALL" 
    ? auctions 
    : auctions.filter((a) => a.status === statusFilter);

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Pending</p>
          <p className="text-2xl font-bold text-amber-600 mt-1">{pendingCount}</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Approved</p>
          <p className="text-2xl font-bold text-green-600 mt-1">{approvedCount}</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Rejected</p>
          <p className="text-2xl font-bold text-red-600 mt-1">{rejectedCount}</p>
        </div>
      </div>

      {/* Filter by Status */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-bold text-[#146C94] mb-4">Filter by Status</h3>
        <div className="flex gap-3 flex-wrap">
          <button
            onClick={() => setStatusFilter("ALL")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "ALL"
                ? "bg-[#19A7CE] text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            All
          </button>
          <button
            onClick={() => setStatusFilter("PENDING")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "PENDING"
                ? "bg-amber-500 text-white"
                : "bg-amber-100 text-amber-800 hover:bg-amber-200"
            }`}
          >
            Pending ({pendingCount})
          </button>
          <button
            onClick={() => setStatusFilter("APPROVED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "APPROVED"
                ? "bg-green-600 text-white"
                : "bg-green-100 text-green-800 hover:bg-green-200"
            }`}
          >
            Approved ({approvedCount})
          </button>
          <button
            onClick={() => setStatusFilter("REJECTED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "REJECTED"
                ? "bg-red-600 text-white"
                : "bg-red-100 text-red-800 hover:bg-red-200"
            }`}
          >
            Rejected ({rejectedCount})
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200 bg-gray-50">
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Image</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Product Name</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Seller</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Starting Price</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Created At</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Status</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredAuctions.map((row) => (
                <tr key={row.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <Image
                      src={row.image}
                      alt={row.name}
                      width={60}
                      height={60}
                      className="w-16 h-16 rounded-lg object-cover"
                      onError={(e) => {
                        e.currentTarget.src = '/default-avatar.png';
                      }}
                    />
                  </td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-700">{row.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{row.seller}</td>
                  <td className="px-6 py-4 text-sm font-semibold text-[#19A7CE]">{formatPrice(row.startingPrice)}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{row.createdAt}</td>
                  <td className="px-6 py-4 text-sm">{getStatusBadge(row.status)}</td>
                  <td className="px-6 py-4 text-sm">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleViewDetails(row)}
                        className="px-3 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200 text-xs font-semibold"
                      >
                        Details
                      </button>
                      <button
                        onClick={() => handleApprove(row)}
                        className="px-3 py-1 bg-green-100 text-green-700 rounded hover:bg-green-200 text-xs font-semibold"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleReject(row)}
                        className="px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 text-xs font-semibold"
                      >
                        Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Auction Detail Modal */}
      {selectedAuction.data && (
        <AuctionDetailModal
          isOpen={selectedAuction.isOpen}
          onClose={() => setSelectedAuction({ isOpen: false, data: null })}
          auctionData={{
            name: selectedAuction.data.name,
            description: selectedAuction.data.description,
            startTime: selectedAuction.data.startTime,
            endTime: selectedAuction.data.endTime,
            images: selectedAuction.data.images,
            startingPrice: selectedAuction.data.startingPrice,
            category: selectedAuction.data.category,
            seller: selectedAuction.data.seller,
          }}
        />
      )}
    </div>
  );
}
