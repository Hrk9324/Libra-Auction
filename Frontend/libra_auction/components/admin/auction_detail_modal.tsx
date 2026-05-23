'use client';

import Image from "next/image";
import AdminModal from "./admin_modal";

interface AuctionDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  auctionData: {
    name: string;
    description: string;
    startTime: string;
    endTime: string;
    images: string[];
    startingPrice: number;
    category: string;
    seller: string;
  };
}

export default function AuctionDetailModal({
  isOpen,
  onClose,
  auctionData,
}: AuctionDetailModalProps) {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price);
  };

  return (
    <AdminModal
      isOpen={isOpen}
      onClose={onClose}
      title={`Auction Details - ${auctionData.name}`}
      size="large"
      footer={
        <div className="flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-300 text-gray-800 rounded font-semibold hover:bg-gray-400 transition"
          >
            Close
          </button>
        </div>
      }
    >
      <div className="space-y-6">
        {/* Basic Info */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Product Name
            </p>
            <p className="text-lg font-bold text-gray-800 mt-1">{auctionData.name}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Category
            </p>
            <p className="text-lg font-bold text-gray-800 mt-1">{auctionData.category}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Seller
            </p>
            <p className="text-lg font-bold text-gray-800 mt-1">{auctionData.seller}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Starting Price
            </p>
            <p className="text-lg font-bold text-[#19A7CE] mt-1">
              {formatPrice(auctionData.startingPrice)}
            </p>
          </div>
        </div>

        {/* Time Info */}
        <div className="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              Start Time
            </p>
            <p className="text-sm text-gray-800 mt-1">{auctionData.startTime}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider">
              End Time
            </p>
            <p className="text-sm text-gray-800 mt-1">{auctionData.endTime}</p>
          </div>
        </div>

        {/* Description */}
        <div>
          <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider mb-2">
            Description
          </p>
          <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 p-4 rounded-lg">
            {auctionData.description}
          </p>
        </div>

        {/* Images Gallery */}
        <div>
          <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider mb-3">
            Product Images
          </p>
          <div className="grid grid-cols-3 gap-3">
            {auctionData.images.map((img, idx) => (
              <div key={idx} className="bg-gray-100 rounded-lg overflow-hidden border border-gray-300">
                <Image
                  src={img}
                  alt={`Product ${idx + 1}`}
                  width={200}
                  height={150}
                  className="w-full h-40 object-cover"
                />
              </div>
            ))}
          </div>
        </div>
      </div>
    </AdminModal>
  );
}
