import Auctions from "@/components/main/auction/auctions";
import { fetchCategories } from "@/services/fetch_categories";
import { notFound } from "next/navigation";

export default async function page(props: {
    params: Promise<{ category_id: string }>;
    searchParams?: { name?: string; status?: string };
}) {
    const params = await props.params;
    const categories = await fetchCategories();

    const category = categories.find((item) => item.id === params.category_id);

    if (!category) {
        notFound();
    }

    return (
        <Auctions
            categoryId={params.category_id}
            categoryName={category.title}
            searchTerm={props.searchParams?.name}
            searchStatus={props.searchParams?.status}
            backHref="/auctions"
        />
    );
}