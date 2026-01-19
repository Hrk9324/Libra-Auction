export default function HomeBannerContentBox() {
  return (
    <div className="w-full h-[76vh] flex flex-col items-start bg-(--background-color)/90 rounded-2xl px-9 py-12">
      <p className="font-semibold tracking-widest">Live Auctions Now Open</p>
      <h1 className="font-extrabold text-[clamp(2.5rem,var(--home-banner-title-font-size),6rem)] text-(--primary-color)">
        Discover and Join Live Online Auctions
      </h1>
      <p className="text-(length:--home-banner-subtitle-font-size)">
        Bid on thousands of premium products in real time. Transparent pricing,
        secure transactions, and fair competition — all in one professional
        auction platform.
      </p>
      <a className="mt-auto bg-(--primary-color) text-white px-12 py-4 font-bold hover:bg-(--primary-color)/90 active:bg-(--primary-color)/80" href="/auctions">
        Join the Auction
      </a>
    </div>
  );
}
