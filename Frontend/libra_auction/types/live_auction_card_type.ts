export type LiveAuctionCardType = {
    id: string,
    image_src: string,
    title: string,
    current_bid: number,
    bids: number,
    time_left: number, // miliseconds left
}