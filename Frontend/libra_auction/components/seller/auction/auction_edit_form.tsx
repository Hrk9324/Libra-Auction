"use client";

import { NewAuction } from "@/types/auction/new-auction";
import { useState, useEffect } from "react";

interface AuctionFormProps {
  initialData?: NewAuction;
  onSubmit: (data: NewAuction) => Promise<void>;
  isUpdating?: boolean;
}

export const AuctionEditForm = ({ initialData, onSubmit, isUpdating }: AuctionFormProps) => {
  const [formData, setFormData] = useState<NewAuction>({
    productId: "",
    startTime: "",
    duration: 3600,
    startingPrice: 0,
    minimumBidIncrement: 0,
    depositAmount: 0
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  const handleChange = (name: string, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setLoading(true);
    try {
      await onSubmit({
        ...formData,
        startTime: new Date(formData.startTime).toISOString()
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-6 bg-white p-8 rounded-2xl border border-gray-100 shadow-sm max-w-2xl">

      <h2 className="text-lg md:text-xl font-bold text-gray-800 border-b pb-4">
        {isUpdating ? "Update auction" : "Create auction"}
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Start time</label>
          <input
            type="datetime-local"
            value={
              formData.startTime
                ? new Date(formData.startTime).toISOString().slice(0, 16)
                : ""
            }
            onChange={(e) => handleChange("startTime", e.target.value)}
            required
            className="p-2.5 border rounded-xl"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Duration (seconds)</label>
          <input
            type="number"
            value={formData.duration}
            onChange={(e) => handleChange("duration", Number(e.target.value))}
            min={1}
            required
            className="p-2.5 border rounded-xl"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Starting price</label>
          <input
            type="number"
            value={formData.startingPrice}
            onChange={(e) => handleChange("startingPrice", Number(e.target.value))}
            min={0}
            required
            className="p-2.5 border rounded-xl"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Minimum bid increment</label>
          <input
            type="number"
            value={formData.minimumBidIncrement}
            onChange={(e) => handleChange("minimumBidIncrement", Number(e.target.value))}
            min={0}
            required
            className="p-2.5 border rounded-xl"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Deposit</label>
          <input
            type="number"
            value={formData.depositAmount}
            onChange={(e) => handleChange("depositAmount", Number(e.target.value))}
            min={0}
            required
            className="p-2.5 border rounded-xl"
          />
        </div>

      </div>

      <div className="flex gap-3 pt-4 border-t">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-(--primary-color) text-white font-bold py-3 rounded-xl disabled:opacity-50"
        >
          {loading ? "Saving..." : "Save changes"}
        </button>
      </div>

    </form>
  );
};
