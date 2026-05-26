'use client';

import type { Category } from "@/types/category";
import { useRouter } from "next/navigation";
import { usePathname, useSearchParams } from "next/navigation";
import { useState } from "react";

const statusOptions = [
    { label: "All", value: "" },
    { label: "Live", value: "DANG_DIEN_RA" },
    { label: "Upcoming", value: "CHUA_BAT_DAU" },
];

export const AuctionFilterSidebar = ({
    categories,
    activeCategoryId = "",
    initialSearchTerm = "",
    initialStatus = "",
}: {
    categories: Category[];
    activeCategoryId?: string;
    initialSearchTerm?: string;
    initialStatus?: string;
}) => {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const [selectedCategoryId, setSelectedCategoryId] = useState(activeCategoryId);
    const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
    const [selectedStatus, setSelectedStatus] = useState(initialStatus);

    const handleConfirmCategory = () => {
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

        const queryString = query.toString();
        const targetPath = selectedCategoryId ? `/auctions/${selectedCategoryId}` : "/auctions";
        router.push(queryString ? `${targetPath}?${queryString}` : targetPath);
    };

    const handleConfirmSearch = () => {
        const query = new URLSearchParams(searchParams.toString());

        if (searchTerm.trim()) {
            query.set("name", searchTerm.trim());
        } else {
            query.delete("name");
        }

        const queryString = query.toString();
        router.push(queryString ? `${pathname}?${queryString}` : pathname);
    };

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

        const queryString = query.toString();
        router.push(queryString ? `${pathname}?${queryString}` : pathname);
    };

    return (
        <div className="space-y-8">
            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Search</h3>
                <div className="space-y-3">
                    <input 
                        type="text" 
                        placeholder="Auction or product name..."
                        value={searchTerm}
                        onChange={(event) => setSearchTerm(event.target.value)}
                        className="w-full pl-3 pr-3 py-2 bg-gray-50 border border-gray-100 rounded-xl text-sm outline-none focus:border-(--primary-color) transition-all"
                    />
                    <button
                        type="button"
                        onClick={handleConfirmSearch}
                        className="w-full rounded-xl bg-(--secondary-color) py-2 text-xs font-semibold uppercase tracking-wider text-white shadow-lg shadow-blue-100 transition-all hover:bg-[#1598bc] active:scale-[0.98] active:bg-[#117f9c]"
                    >
                        CONFIRM SEARCH
                    </button>
                </div>
            </div>

            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Categories</h3>
                <div className="space-y-3">
                    <select
                        value={selectedCategoryId}
                        onChange={(event) => setSelectedCategoryId(event.target.value)}
                        className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm outline-none transition-all focus:border-(--primary-color)"
                    >
                        <option value="">Tất cả</option>
                        {categories.map((category) => (
                            <option key={category.id} value={category.id}>
                                {category.title}
                            </option>
                        ))}
                    </select>

                    <button
                        type="button"
                        onClick={handleConfirmCategory}
                        className="w-full rounded-xl bg-(--secondary-color) py-2 text-xs font-semibold uppercase tracking-wider text-white shadow-lg shadow-blue-100 transition-all hover:bg-[#1598bc] active:scale-[0.98] active:bg-[#117f9c] disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        CONFIRM CATEGORY
                    </button>
                </div>
            </div>

            <div>
                <h3 className="font-bold text-[#146C94] mb-4 uppercase text-xs tracking-widest">Price range (VND)</h3>
                <div className="grid grid-cols-2 gap-2">
                    <input type="number" placeholder="From" className="w-full p-2 bg-gray-50 border border-gray-100 rounded-lg text-xs outline-none" />
                    <input type="number" placeholder="To" className="w-full p-2 bg-gray-50 border border-gray-100 rounded-lg text-xs outline-none" />
                </div>
            </div>

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

            <button
                type="button"
                onClick={handleApplyFilters}
                className="w-full rounded-xl py-2 bg-(--secondary-color) text-xs font-semibold uppercase tracking-wider text-white shadow-lg shadow-blue-100 transition-all hover:bg-[#1598bc] active:scale-[0.98] active:bg-[#117f9c]"
            >
                APPLY FILTERS
            </button>
        </div>
    );
};