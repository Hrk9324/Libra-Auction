import BreadCrumb from "@/components/main/auction/breadcrumb";
import { fetchPublicAuction } from "@/services/fetch_public_auction";
import { checkRegistration } from "@/services/register_auction";
import { getIdFromToken } from "@/lib/get_id_from_token";
import { notFound } from "next/navigation";
import LiveAuctionView from "@/components/main/auction/live_auction_view";

export default async function LivePage(props: {
  params: Promise<{ category_id: string; auction_id: string }>;
}) {
  const params = await props.params;
  const auctionId = params.auction_id;
  const auction = await fetchPublicAuction(auctionId);
  if (!auction) notFound();

  const backendServerUrl = process.env.PUBLIC_BACKEND_SERVER_URL || process.env.BACKEND_SERVER_URL || '';

  // Check if current user is registered and not the creator
  let isRegistered = false;
  let isCreator = false;
  try {
    const userId = await getIdFromToken();
    if (userId) {
      if (auction.creator_id && auction.creator_id === userId) {
        isCreator = true;
      }
      const registration = await checkRegistration(userId, auctionId);
      isRegistered = !!registration;
    }
  } catch {
    // Not logged in or error
  }

  const breadcrumb_items = [
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
      id: `${auction.auction_id}-live`,
      value: "Live",
      href: `/auctions/${auction.category_id}/${auction.auction_id}/live`,
    },
  ];

  return (
    <>
      <BreadCrumb breadcrumbItems={breadcrumb_items} />
      <LiveAuctionView
        auction={auction}
        backendServerUrl={backendServerUrl}
        role="user"
        isRegistered={isRegistered}
        isCreator={isCreator}
      />
    </>
  );
}
