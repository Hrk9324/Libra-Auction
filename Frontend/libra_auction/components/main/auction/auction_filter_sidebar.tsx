'use client';

import type { Category } from "@/types/category";
import { useRouter } from "next/navigation";
import { useSearchParams } from "next/navigation";
import { useState } from "react";

const statusOptions = [
    { label: "All", value: "" },
    { label: "Live", value: "IN_PROGRESS" },
    { label: "Upcoming", value: "NOT_STARTED" },
];

export const AuctionFilterSidebar = ({
    categories,
    activeCategoryId = "",
    initialSearchTerm = "",
    initialStatus = "",
    initialPriceFrom = "",
    initialPriceTo = "",
}: {
    categories: Category[];
    activeCategoryId?: string;
    initialSearchTerm?: string;
    initialStatus?: string;
    initialPriceFrom?: string;
    initialPriceTo?: string;
}) => {
    const router = useRouter();
    const searchParams = useSearchParams();
    
    // Lưu các lựa chọn vào state tạm thời
    const [selectedCategoryId, setSelectedCategoryId] = useState(activeCategoryId);
    const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
    const [selectedStatus, setSelectedStatus] = useState(initialStatus);
    const [priceFrom, setPriceFrom] = useState(initialPriceFrom);
    const [priceTo, setPriceTo] = useState(initialPriceTo);

    // Xử lý gom toàn bộ filter và đẩy lên URL khi bấm nút Apply
    const handleApplyFilters = () => {
        const query = new URLSearchParams(searchParams.toString());

        if (searchTerm.trim()) {
            query.set("name", searchTerm.trim());
        } else {
            query.delete("name");
        }

        if (selectedStatus) {
            query.set("status", selectedStatus);
        } else {
            query.delete("status");
        }

        if (priceFrom.trim()) {
            query.set("priceFrom", priceFrom.trim());
        } else {
            query.delete("priceFrom");
        }

        if (priceTo.trim()) {
            query.set("priceTo", priceTo.trim());
        } else {
            query.delete("priceTo");
        }

        const queryString = query.toString();
        
        // Nếu chọn danh mục cụ thể thì trỏ sang đường dẫn động, ngược lại về /auctions tổng quát
        const targetPath = selectedCategoryId ? `/auctions/${selectedCategoryId}` : "/auctions";
        
        router.push(queryString ? `${targetPath}?${queryString}` : targetPath);
    };

    return (
        <div className="space-y-8">
            {/* SEARCH */}
            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Search</h3>
                <input 
                    type="text" 
                    placeholder="Auction or product name..."
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.target.value)}
                    className="w-full pl-3 pr-3 py-2 bg-gray-50 border border-gray-100 rounded-xl text-sm outline-none focus:border-(--primary-color) transition-all"
                />
            </div>

            {/* CATEGORIES */}
            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Categories</h3>
                <select
                    value={selectedCategoryId}
                    onChange={(event) => setSelectedCategoryId(event.target.value)}
                    className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm outline-none transition-all focus:border-(--primary-color)"
                >
                    <option value="">All</option>
                    {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                            {category.title}
                        </option>
                    ))}
                </select>
            </div>

            {/* PRICE RANGE */}
            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Price range (VND)</h3>
                <div className="grid grid-cols-2 gap-2">
                    <input
                        type="number"
                        placeholder="From"
                        value={priceFrom}
                        onChange={(event) => setPriceFrom(event.target.value)}
                        className="w-full rounded-lg border border-gray-100 bg-gray-50 p-2 text-xs outline-none"
                    />
                    <input
                        type="number"
                        placeholder="To"
                        value={priceTo}
                        onChange={(event) => setPriceTo(event.target.value)}
                        className="w-full rounded-lg border border-gray-100 bg-gray-50 p-2 text-xs outline-none"
                    />
                </div>
            </div>

            {/* STATUS */}
            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Status</h3>
                <div className="flex flex-wrap gap-2">
                    {statusOptions.map((status) => {
                        const isActive = selectedStatus === status.value;

                        return (
                            <button
                                key={status.label}
                                type="button"
                                onClick={() => setSelectedStatus(status.value)}
                                className={`rounded-xl border px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider transition-all ${
                                    isActive
                                        ? "border-(--primary-color) bg-(--primary-color) text-white shadow-sm"
                                        : "border-gray-200 bg-white text-gray-800 hover:bg-(--primary-color) hover:text-white"
                                }`}
                            >
                                {status.label}
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* TỔNG HỢP FILTER BUTTON */}
            <button
                type="button"
                onClick={handleApplyFilters}
                className="w-full rounded-xl py-3 bg-(--secondary-color) text-xs font-bold uppercase tracking-wider text-white shadow-lg shadow-blue-100 transition-all hover:bg-[#1598bc] active:scale-[0.98] active:bg-[#117f9c]"
            >
                APPLY FILTERS
            </button>
        </div>
    );
};