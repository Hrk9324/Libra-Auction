"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";
import { ProductDetail } from "@/components/seller/product/product_detail";
import { Product } from "@/types/product/product";
import { fetchProduct } from "@/services/fetch_product";

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    const timer = setTimeout(async () => {
      if (!params.product_id || Array.isArray(params.product_id)) {
        setLoading(false);
        return;
      }

      try {
        const product = await fetchProduct(params.product_id);
        setProduct(product);
      } catch (fetchError) {
        setError(fetchError);
      } finally {
        setLoading(false);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [params.product_id]);

  if (loading) return <div className="p-10 text-center text-gray-400 italic">Loading product...</div>;
  if (error) {
    const status = getErrorStatus(error);
    return <ErrorView status={status} title={getErrorTitle(status)} message={getErrorMessage(error)} />;
  }
  if (!product) return <ErrorView status={404} title="Product not found" />;

  return (
    <main className="p-6 md:p-10 bg-(--background-color) min-h-screen">
      <div className="max-w-6xl mx-auto">
        <button
          onClick={() => router.push("/seller-dashboard/products")}
          className="mb-6 flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-(--primary-color) hover:cursor-pointer transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m15 18-6-6 6-6" /></svg>
          Product list
        </button>

        <ProductDetail data={product} />
      </div>
    </main>
  );
}