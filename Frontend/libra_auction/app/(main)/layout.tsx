import type { Metadata } from "next";
import Header from "@/components/main/header";
import Footer from "@/components/main/footer";
export const metadata: Metadata = {
  title: "Libra Auction",
  description: "An Online Auction System",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="flex flex-col min-h-screen">
      <header className="fixed h-(--header-height) w-full z-(--header-z-index)">
        <Header />
      </header>
      <div id="page" className="pt-(--header-height) flex-1">
        {children}
      </div>
      <footer className="mt-auto">
        <Footer />
      </footer>
    </div>
  );
}
