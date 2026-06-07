import AuctionForm from "@/components/seller/auction/auctionForm";
import { fetchProducts } from "@/services/fetch_products";

export default async function page() {
    const products = await fetchProducts();
    return (
        <AuctionForm products={products} />
    );
}