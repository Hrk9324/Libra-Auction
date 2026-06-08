"use client";
import Image from "next/image";
import { useState } from "react";

export const ImageGallery = ({ images }: { images: string[] }) => {
  const [mainImage, setMainImage] = useState(images[0] || "/placeholder-product.png");

  return (
    <div className="flex flex-col gap-4">
      {/* Ảnh chính */}
      <div className="relative aspect-square w-full rounded-2xl overflow-hidden border border-gray-100 bg-white">
        <Image
          src={mainImage}
          alt="Product"
          fill
          className="object-contain p-4"
        />
      </div>

      {/* Danh sách ảnh thu nhỏ */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {images.map((img, idx) => (
          <button
            key={idx}
            onClick={() => setMainImage(img)}
            className={`flex-shrink-0 w-20 h-20 rounded-lg border-2 transition-all overflow-hidden bg-white ${mainImage === img ? "border-[var(--primary-color)]" : "border-transparent"
              }`}
          >
            <div className="relative h-full w-full">
              <Image src={img} alt={`Thumb ${idx}`} fill className="object-cover" />
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};