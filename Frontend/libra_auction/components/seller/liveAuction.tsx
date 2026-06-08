"use client";
import { useEffect, useState } from "react";
import { auctionSocket } from "@/services/auction_socket";

interface Bid {
  id: number;
  bidder: string;
  amount: number;
  time: string;
}

export default function LiveAuction({ auctionId }: { auctionId: string }) {
  const [bids, setBids] = useState<Bid[]>([]);
  const [currentPrice, setCurrentPrice] = useState(8500);
  const timeLeft = "12:45:02";

  useEffect(() => {
    auctionSocket.connect(auctionId);

    // Subscribe to new bids
    auctionSocket.subscribe(`/topic/auction/${auctionId}/bids`, (newBid: Bid) => {
      setBids(prev => [newBid, ...prev]);
      setCurrentPrice(newBid.amount);
    });

    return () => auctionSocket.unsubscribe(`/topic/auction/${auctionId}/bids`);
  }, [auctionId]);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 bg-[#F6F1F1] p-4 rounded-xl">
      {/* Main information */}
      <div className="lg:col-span-2 space-y-4">
        <div className="bg-white p-6 rounded-lg border border-[#AFD3E2] shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-xl font-bold text-[#146C94]">Vintage Rolex Submariner</h2>
              <p className="text-sm text-gray-500">ID: #{auctionId}</p>
            </div>
            <div className="text-right">
              <p className="text-xs uppercase font-bold text-red-500 animate-pulse">● Live</p>
              <p className="text-2xl font-mono font-bold text-[#146C94]">{timeLeft}</p>
            </div>
          </div>

          <div className="mt-8 p-4 bg-[#F6F1F1] rounded flex justify-between items-center">
            <span className="text-gray-600">Current price:</span>
            <span className="text-4xl font-black text-[#19A7CE]">${currentPrice.toLocaleString()}</span>
          </div>
        </div>

        {/* Mini price chart */}
        <div className="bg-white p-4 rounded-lg border border-[#AFD3E2] h-32 flex items-end gap-1">
          {bids.slice(0, 20).reverse().map((b, i) => (
            <div 
              key={i} 
              className="flex-1 bg-[#AFD3E2] hover:bg-[#19A7CE] transition-all" 
              style={{ height: `${(b.amount / currentPrice) * 100}%` }}
            />
          ))}
        </div>
      </div>

      {/* Realtime bid list */}
      <div className="bg-white rounded-lg border border-[#AFD3E2] flex flex-col h-[500px]">
        <div className="p-4 border-b border-[#AFD3E2] font-bold text-[#146C94]">
          Bid history
        </div>
        <div className="flex-1 overflow-y-auto p-2 space-y-2">
          {bids.length === 0 && <p className="text-center text-gray-400 py-10">No bids yet...</p>}
          {bids.map((bid) => (
            <div key={bid.id} className="flex justify-between items-center p-3 bg-[#F6F1F1] rounded border-l-4 border-[#19A7CE] animate-in slide-in-from-right-4">
              <div>
                <p className="text-sm font-bold">{bid.bidder}</p>
                <p className="text-[10px] text-gray-400">{bid.time}</p>
              </div>
              <span className="font-bold text-[#146C94]">+${bid.amount.toLocaleString()}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}