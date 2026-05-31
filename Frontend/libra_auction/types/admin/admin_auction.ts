export interface AdminAuction {
    category_id: string;
    category_name: string;
    auction_id: string;
    auction_name: string;
    auction_status: string;
    approval_status: string;
    start_time: string;
    duration: number;
    starting_price: number;
    current_price: number;
    deposit_amount: number;
    min_bid_increment: number;
    product_id: string;
    product_name: string;
    quantity: number;
    description: string;
    images: string[];
    attributes: Array<{
        name: string;
        value: string;
    }>;
    total_bids: number;
    total_participants: number;
    failure_reason?: string;
    completed_at?: string;
}
