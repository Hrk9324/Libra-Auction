import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/header";
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
    <>
      <html lang="en">
        <body>
          <header className="fixed w-full z-(--header-z-index)">
            <Header />
          </header>
          <div id="page" className="pt-(--header-height)">
            {children}
          </div>
        </body>
      </html>
    </>
  );
}
