import { redirect, notFound } from "next/navigation";
import { fetchPublicAuction } from "@/services/fetch_public_auction";
import { checkRegistration } from "@/services/register_auction";
import { fetchUserInfo } from "@/services/fetch_user_info";
import { getIdFromToken } from "@/lib/get_id_from_token";
import { Auction } from "@/types/auction/auction";
import BreadCrumb from "@/components/main/auction/breadcrumb";
import DepositPaymentSection from "@/components/main/auction/deposit_payment_section";
import { CurrencyFormat } from "@/utils/currency_format";

export default async function RegistrationPage(props: {
    params: Promise<{ category_id: string; auction_id: string }>;
    searchParams: Promise<{ status?: string }>;
}) {
    const params = await props.params;
    const searchParams = await props.searchParams;
    const auctionId = params.auction_id;
    const categoryId = params.category_id;
    const paymentStatus = searchParams.status || null;

    // Check authentication
    const userId = await getIdFromToken();
    if (!userId) {
        redirect("/sign-in");
    }

    // Fetch auction data
    let auction: Auction;
    try {
        auction = await fetchPublicAuction(auctionId);
    } catch {
        notFound();
    }

    // Check registration status
    const registration = await checkRegistration(userId, auctionId);
    if (!registration) {
        redirect(`/auctions/${categoryId}/${auctionId}`);
    }

    // Fetch user info
    const userInfo = await fetchUserInfo(userId);

    const breadcrumbItems = [
        {
            id: auction.category_id,
            value: auction.category_name,
            href: `/auctions/${auction.category_id}`,
        },
        {
            id: auction.auction_id,
            value: auction.product_name,
            href: `/auctions/${auction.category_id}/${auction.auction_id}`,
        },
        {
            id: "registration",
            value: "Đăng ký tham gia",
            href: `/auctions/${auction.category_id}/${auction.auction_id}/registration`,
        },
    ];

    return (
        <>
            <BreadCrumb breadcrumbItems={breadcrumbItems} />
            <div className="max-w-2xl mx-auto py-8 px-4">
                <h1 className="text-2xl font-bold text-[#146C94] mb-6">
                    Thông tin đăng ký đấu giá
                </h1>

                {/* Registration Info */}
                <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">
                        Phiên đấu giá
                    </h2>
                    <div className="space-y-3">
                        <div className="flex justify-between">
                            <span className="text-gray-500">Sản phẩm:</span>
                            <span className="font-medium text-gray-800">
                                {auction.product_name}
                            </span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">Giá khởi điểm:</span>
                            <span className="font-medium text-gray-800">
                                {CurrencyFormat(auction.starting_price)}
                            </span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">Người đăng ký:</span>
                            <span className="font-medium text-gray-800">
                                {userInfo.fullName}
                            </span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">Email:</span>
                            <span className="font-medium text-gray-800">
                                {userInfo.email}
                            </span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">Thời gian đăng ký:</span>
                            <span className="font-medium text-gray-800">
                                {new Intl.DateTimeFormat('vi-VN', {
                                    day: '2-digit',
                                    month: '2-digit',
                                    year: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit',
                                }).format(new Date(registration.registrationTime))}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Deposit Payment */}
                <DepositPaymentSection
                    auction={auction}
                    paymentStatus={paymentStatus}
                />
            </div>
        </>
    );
}
