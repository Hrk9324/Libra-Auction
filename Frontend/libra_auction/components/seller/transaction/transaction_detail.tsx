import { TransactionDetailData } from "@/types/transaction_detail_type";

const statusConfig = {
  SUCCESS: { label: "Success", color: "bg-green-100 text-green-700 border-green-200" },
  PENDING: { label: "Pending", color: "bg-amber-100 text-amber-700 border-amber-200" },
  FAILED: { label: "Failed", color: "bg-red-100 text-red-700 border-red-200" },
};

interface DetailItemProps {
  label: string;
  value: string;
  highlight?: boolean;
  isLink?: boolean;
  href?: string;
}

export const TransactionDetail = ({ data }: { data: TransactionDetailData }) => {
  const status = statusConfig[data.status];

  return (
    <div className="bg-white rounded-2xl border border-[#AFD3E2] shadow-sm overflow-hidden max-w-2xl mx-auto">
      {/* Card header */}
      <div className="bg-[#F6F1F1] px-6 py-4 border-b border-[#AFD3E2] flex justify-between items-center">
        <div>
          <p className="text-[10px] font-bold text-[#146C94] uppercase tracking-widest">Transaction details</p>
          <h2 className="text-sm font-mono font-bold text-gray-700 mt-0.5">#{data.id}</h2>
        </div>
        <span className={`text-[10px] font-black uppercase px-3 py-1 rounded-full border ${status.color}`}>
          {status.label}
        </span>
      </div>

      {/* Key-value list */}
      <div className="p-6 space-y-4">
        <div className="grid grid-cols-1 gap-4">
          <DetailItem label="Auction" value={data.auction_name} isLink href={`/auctions/${data.auction_id}`} />
          <DetailItem label="Participant" value={data.participant_name} />
          <DetailItem 
            label="Payment amount" 
            value={`${data.amount.toLocaleString()} VND`} 
            highlight 
          />
          <DetailItem label="Transaction time" value={data.created_at} />
          <DetailItem label="Method" value={data.payment_method} />
          
          {data.transaction_hash && (
            <div className="pt-2">
              <p className="text-[10px] font-bold text-gray-400 uppercase mb-1">System reference ID</p>
              <p className="text-xs font-mono bg-gray-50 p-2 rounded border border-gray-100 break-all text-gray-500">
                {data.transaction_hash}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Footer with guidance or notes */}
      <div className="px-6 py-4 bg-gray-50 border-t border-gray-100">
        <p className="text-[11px] text-gray-400 italic text-center">
          This transaction was recorded automatically by the payment system.
        </p>
      </div>
    </div>
  );
};

// Helper component for key-value rows
const DetailItem = ({ label, value, highlight, isLink, href }: DetailItemProps) => (
  <div className="flex justify-between items-start py-2 border-b border-gray-50 last:border-0">
    <span className="text-xs font-bold text-gray-400 uppercase tracking-tight">{label}</span>
    {isLink ? (
      <a href={href} className="text-sm font-semibold text-[#19A7CE] hover:underline transition-all">
        {value}
      </a>
    ) : (
      <span className={`text-sm font-semibold ${highlight ? 'text-[#146C94] text-base' : 'text-gray-800'}`}>
        {value}
      </span>
    )}
  </div>
);