"use client";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";
import ProductDeleteConfirm from "@/components/seller/product_delete_confirm";
import { Product } from "@/types/product/product";
import { fetchProduct } from "@/services/fetch_product";
import { deleteProduct } from "@/services/delete_product";

export default function DeleteProductPage() {
  const params = useParams();

  const [product, setProduct] = useState<Product | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  // 1. Fetch product
  useEffect(() => {
    const loadProduct = async () => {
      if (!params.product_id || Array.isArray(params.product_id)) {
        setIsLoading(false);
        return;
      }

      try {
        const data = await fetchProduct(params.product_id);
        setProduct(data);
      } catch (err) {
        setError(err);
      } finally {
        setIsLoading(false);
      }
    };

    loadProduct();
  }, [params.product_id]);

  // 2. Handle DELETE
  const handleDelete = async () => {
    if (!params.product_id || Array.isArray(params.product_id)) return;

    try {
      setDeleteError(null);
      await deleteProduct(params.product_id);
      window.location.replace("/seller-dashboard/products")
    } catch (err) {
      setDeleteError(getErrorMessage(err, "Failed to delete product!"));
    }
  };

  // UI loading
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F6F1F1]">
        <div className="text-[#146C94] font-bold animate-pulse">
          Loading information...
        </div>
      </div>
    );
  }

  if (error) {
    const status = getErrorStatus(error);
    return <ErrorView status={status} title={getErrorTitle(status)} message={getErrorMessage(error)} />;
  }

  // không có data
  if (!product) return <ErrorView status={404} title="Product not found" />;

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#F6F1F1] p-6">
      <div className="w-full max-w-2xl space-y-4">
        {deleteError ? (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{deleteError}</p>
        ) : null}
        <ProductDeleteConfirm
          product={product}
          onDelete={handleDelete}
          onCancel={() => {
            window.location.replace("/seller-dashboard/products");
          }}
        />
      </div>
    </div>
  );
}