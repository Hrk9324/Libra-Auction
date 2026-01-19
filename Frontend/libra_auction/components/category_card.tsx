
import type { CategoryCardType } from "@/types/category_card_type";
import Image from "next/image";
export default function CategoryCard({ card }: { card: CategoryCardType }) {
  return (
    <div className="w-full">
      <a href={card.href}>
        <div className="flex flex-col items-center gap-4">
          <div className="relative h-(--home-categories-section-image-height) w-full rounded-2xl overflow-hidden">
            <Image src={card.image_src} alt="" layout="fill" className="h-full object-cover"/>
          </div>
          <p className="capitalize font-semibold">{card.title}</p>
        </div>
      </a>
    </div>
  );
}
