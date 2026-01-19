import { FetchLiveAuctions } from "@/services/fetch_live_auctions";
import LiveAuctionCard from "./live_auction_card";
import ShowMoreBtn from "./show_more_btn";
export default function LiveAuctionsSection() {
  const cards = FetchLiveAuctions();
  return (
    <div className="py-16 bg-(--background-color)">
      <div className="flex flex-col items-center gap-4">
        <h2 className="font-bold text-4xl">Live Auctions</h2>
        <p>Join Live Auctions Happening Right Now</p>
      </div>
      <div className="flex gap-9 justify-center pt-6 px-18">
        {cards.map((card) => {
          return <LiveAuctionCard key={card.id} card={card}/>;
        })}
      </div>
      <div className="flex justify-center pt-18">
        <ShowMoreBtn/>
      </div>
    </div>
  );
}
