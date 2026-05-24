"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AuctionDetail } from "@/components/seller/auction/auction_detail";
import { Auction } from "@/types/auction/auction";
import { fetchAuction } from "@/services/fetch_auction";

export default function Page() {
    const params = useParams();
    const router = useRouter();
    const [data, setData] = useState<Auction | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                if (!params.auction_id || Array.isArray(params.auction_id)) {
                    return;
                }
                const data = await fetchAuction(params.auction_id);
                if (data) {
                    setData(data);
                }
            } catch (err) {
                console.error("Fetch error:", err);
                setData(null);
            } finally {
                setLoading(false);
            }
        };

        if (params.auction_id) {
            fetchData();
        }
    }, [params.auction_id]);

    if (loading) return <div className="p-10 text-center text-gray-400 italic">Loading auction...</div>;

    return (
        <main className="p-6 md:p-10 bg-(--background-color) min-h-screen">
            <div className="max-w-6xl mx-auto">
                <button
                    onClick={() => router.push("/seller-dashboard/auctions")}
                    className="mb-6 flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-(--primary-color) hover:cursor-pointer transition-colors"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m15 18-6-6 6-6" /></svg>
                    Auction list
                </button>

            {data ? (
                <AuctionDetail data={data} />
            ) : (
                <p>Auction not found</p>
            )}
            </div>
        </main>
    );
}