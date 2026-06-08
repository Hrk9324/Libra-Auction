import logo_img from "@/public/logo.png";
import Link from "next/link";
import Image from "next/image";

export default function Footer() {
  // Định nghĩa các nhóm liên kết tương tự cấu trúc của Header
  const supportItems = [
    { value: "Help Center", href: "/help" },
    { value: "Contact Us", href: "/contact" },
    { value: "Privacy Policy", href: "/privacy" },
    { value: "Terms of Service", href: "/terms" },
  ];

  const quickLinks = [
    { value: "Browse Auctions", href: "/auctions" },
    { value: "Sell Your Item", href: "/sell" },
    { value: "How to Bid", href: "/how-to-bid" },
    { value: "Payment Methods", href: "/payments" },
  ];

  const categoryItems = [
    { value: "Vehicle Auction", href: "/categories/vehicles" },
    { value: "Collectibles Auction", href: "/categories/collectibles" },
    { value: "Real Estate Auction", href: "/categories/real-estate" },
    { value: "Fine Art Auction", href: "/categories/fine-art" },
  ];

  return (
    <footer className="p-8 pt-12 bg-(--secondary-color) text-white">
      <div className="flex mb-4 flex-wrap lg:flex-nowrap gap-10">
        {/* --- BRAND INFO --- */}
        <div className="flex flex-1 flex-col gap-2">
          {/* --- LOGO --- */}
          <Link href="/" className="flex items-center gap-2 flex-1">
            <Image src={logo_img} alt="Libra Auction" className="h-8 w-8" />
            <span className="text-xl font-bold text-white">
              Libra Auction
            </span>
          </Link>
          <p className="max-w-md">
            A trusted online auction platform connecting buyers and sellers
            through secure, transparent, and real-time bidding.
          </p>
          <p className="mt-2 font-medium">support@libraauction.com</p>
        </div>

        {/* --- NAVIGATION LINKS --- */}
        <div className="flex gap-12 md:gap-24 mx-0 md:mx-8 flex-wrap">
          {/* Section: Support */}
          <div className="flex flex-col gap-2">
            <p className="font-semibold text-lg mb-2">Support</p>
            {supportItems.map((item) => (
              <Link key={item.value} href={item.href} className="hover:underline opacity-80 hover:opacity-100 transition">
                {item.value}
              </Link>
            ))}
          </div>

          {/* Section: Quick Links */}
          <div className="flex flex-col gap-2">
            <p className="font-semibold text-lg mb-2">Quick Links</p>
            {quickLinks.map((item) => (
              <Link key={item.value} href={item.href} className="hover:underline opacity-80 hover:opacity-100 transition">
                {item.value}
              </Link>
            ))}
          </div>

          {/* Section: Categories */}
          <div className="flex flex-col gap-2">
            <p className="font-semibold text-lg mb-2">Categories</p>
            {categoryItems.map((item) => (
              <Link key={item.value} href={item.href} className="hover:underline opacity-80 hover:opacity-100 transition">
                {item.value}
              </Link>
            ))}
          </div>
        </div>
      </div>

      {/* --- COPYRIGHT --- */}
      <div className="pt-4 border-t border-white/20 mt-8">
        <p className="opacity-60">© 2026 LibraAuction. All rights reserved.</p>
      </div>
    </footer>
  );
}