import { Attribute } from "../product/attribute";
import { ApprovalStatus, AuctionStatus } from "../status";

export interface Auction {
        category_id: string,
        category_name: string,
        auction_id: string,
        auction_name: string,
        auction_status: AuctionStatus,
        approval_status: ApprovalStatus,
        start_time: Date,
        duration: number,

        starting_price: number,
        current_price: number,
        deposit_amount: number,
        min_bid_increment: number,

        product_id: string,
        product_name: string,
        quantity: number,
        description: string,

        images: string[],
        attributes: Attribute[],

        total_bids: number,
        total_participants: number,
        failure_reason?: string,
        completed_at?: string,
        creator_id?: string
}
