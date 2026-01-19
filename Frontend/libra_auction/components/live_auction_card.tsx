import type { LiveAuctionCardType } from "@/types/live_auction_card_type";
import { DurationFormat } from "@/utils/duration_format";
import { CurrencyFormat } from "@/utils/currency_format";
import Image from "next/image";
export default function LiveAuctionCard({
  card,
}: {
  card: LiveAuctionCardType;
}) {
  return (
    <div className="flex flex-col size-full rounded-2xl overflow-hidden shadow-2xl">
      <div className="relative h-(--home-live-auction-section-image-height) shadow-[0_6px_15px_rgba(0,0,0,0.08)]">
        <div className="absolute size-full">
          <div className="relative size-full">
            <Image
              src={card.image_src}
              alt=""
              layout="fill"
              className="h-full object-cover"
            />
          </div>
        </div>
        <div className="absolute size-full">
          <div className="size-full flex flex-col justify-around p-4">
            <div className="flex">
              <div className="bg-red-500 p2 rounded-sm px-4 py-1 text-white font-bold uppercase">
                Live
              </div>
            </div>
            <div className="flex-1"></div>
            <div className="flex">
              <p className="bg-(--time-left-box-color) [--time-left-box-color:#AFD3E2] p2 rounded-sm px-4 py-1 text-white font-bold lowercase">
                {DurationFormat(card.time_left)}
              </p>
            </div>
          </div>
        </div>
      </div>
      <div className="h-full bg-white p-6 flex flex-col gap-4">
        <p className="font-bold text-2xl uppercase truncate">{card.title}</p>
        <div className="flex border-b border-b-gray-500 pb-3">
          <p className="text-lg">Current Bid</p>
          <div className="flex-1"></div>
          <p className="text-xl font-bold">
            {CurrencyFormat(card.current_bid)}
          </p>
        </div>
        <div className="flex pt-1">
          <p className="text-lg">{card.bids} Bids</p>
          <div className="flex-1"></div>
          <a className="mt-auto bg-(--primary-color) text-white px-6 py-2 font-bold hover:bg-(--primary-color)/90 active:bg-(--primary-color)/80" href={`auctions/${card.id}`}>
            Place Bid
          </a>
        </div>
      </div>
    </div>
  );
}
