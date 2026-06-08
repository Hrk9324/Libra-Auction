import Link from "next/link";
import Image from "next/image";
import logo_img from "@/public/logo.png";

const menuItems = [
  { name: 'Dashboard', href: '/seller-dashboard/' },
  { name: 'Auctions', href: '/seller-dashboard/auctions' },
  { name: 'Products', href: '/seller-dashboard/products' },
  // TODO: transactions
  // { name: 'Transactions', href: '/seller-dashboard/transactions' }, 
];

export default function Sidebar() {
  return (
    <aside className="w-64 bg-[#146C94] text-white min-h-screen p-4 flex flex-col">
      <div className="text-2xl font-bold mb-8 p-2">
      <Link href="/" className="flex items-center gap-2 flex-1">
        <Image src={logo_img} alt="Libra Auction" className="h-8 w-8" />
        <span className="text-xl font-bold text-white">
          Libra Auction
        </span>
      </Link>
        </div>
      <nav className="flex-1">
        {menuItems.map((item) => (
          <a
            key={item.name}
            href={item.href}
            className="block p-3 mb-2 rounded hover:bg-[#19A7CE] transition-colors"
          >
            {item.name}
          </a>
        ))}
      </nav>
    </aside>
  );
}