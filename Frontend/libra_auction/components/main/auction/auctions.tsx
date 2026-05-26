import { fetchPublicAuctions } from "@/services/fetch_public_auctions"
import { fetchCategories } from "@/services/fetch_categories";
import AuctionCard from "./auction_card";
import { AuctionFilterSidebar } from "@/components/main/auction/auction_filter_sidebar";

export default async function Auctions({
    categoryId,
    categoryName,
    searchTerm,
    searchStatus,
    backHref,
}: {
    categoryId?: string;
    categoryName?: string;
    searchTerm?: string;
    searchStatus?: string;
    backHref?: string;
}) {
    const [cards, categories] = await Promise.all([
        fetchPublicAuctions(categoryId, searchTerm, searchStatus),
        fetchCategories(),
    ]);

    const pageTitle = categoryName || "Online Auction Marketplace";
    const pageDescription = categoryName
        ? `Found ${cards.length} auctions in this category`
        : `Found ${cards.length} live auctions`;

    return (
        <div className="flex min-h-screen bg-(--background-color)">
            {/* Sidebar */}
            <aside className="w-1/4 sticky top-0 h-screen border-r border-gray-100 bg-white p-6 hidden md:block">
                <AuctionFilterSidebar
                    categories={categories}
                    activeCategoryId={categoryId}
                    initialSearchTerm={searchTerm}
                    initialStatus={searchStatus}
                />
            </aside>

            {/* Main content */}
            <main className="w-full md:w-3/4 p-6 lg:p-8">
                <header className="flex justify-between items-end mb-8">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-800">{pageTitle}</h1>
                        <p className="text-sm text-gray-500 mt-1">{pageDescription}</p>
                    </div>
                    
                    {/* Sort dropdown */}
                    <select className="bg-white text-sm border border-gray-200 rounded-lg p-2 outline-none focus:ring-1 focus:ring-(--primary-color)">
                        <option>Newest</option>
                        <option>Lowest price</option>
                        <option>Highest price</option>
                        <option>Ending soon</option>
                    </select>
                </header>

                {/* Grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {cards.map((card) => (
                        <AuctionCard key={card.auction_id} auctionCard={card} />
                    ))}
                </div>

                {/* Empty state */}
                {cards.length === 0 && (
                    <div className="text-center py-20 bg-white rounded-3xl border-2 border-dashed border-gray-100">
                        <p className="text-gray-400">No matching auctions found.</p>
                        {backHref && (
                            <a
                                href={backHref}
                                className="mt-4 inline-flex items-center justify-center rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-700 transition-colors hover:border-[#19A7CE] hover:text-[#19A7CE]"
                            >
                                Back to all auctions
                            </a>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}