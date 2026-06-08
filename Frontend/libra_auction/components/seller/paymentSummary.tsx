"use client";

const paymentHistory = [
  { id: "PAY-001", auction: "Vintage Rolex", amount: 8500, status: "Completed", date: "2024-03-15" },
  { id: "PAY-002", auction: "MacBook Pro M2", amount: 2100, status: "Processing", date: "2024-03-18" },
  { id: "PAY-003", auction: "iPhone 15 Pro", amount: 999, status: "Pending", date: "2024-03-20" },
];

export default function PaymentSummary() {
  return (
    <div className="space-y-6">
      {/* Financial overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-6 bg-[#146C94] text-white rounded-lg shadow-sm">
          <p className="text-sm opacity-80 font-medium">Available balance</p>
          <p className="text-3xl font-bold mt-2">$10,600.00</p>
          <button className="mt-4 w-full bg-white text-[#146C94] py-2 rounded text-sm font-bold hover:bg-[#F6F1F1] transition-colors">
            Withdraw
          </button>
        </div>

        <div className="p-6 bg-white border border-[#AFD3E2] rounded-lg">
          <p className="text-sm text-gray-500 font-medium">Pending</p>
          <p className="text-3xl font-bold mt-2 text-[#19A7CE]">$3,099.00</p>
          <p className="text-[10px] text-gray-400 mt-2">* Funds become available after buyer confirmation</p>
        </div>

        <div className="p-6 bg-white border border-[#AFD3E2] rounded-lg">
          <p className="text-sm text-gray-500 font-medium">Total revenue</p>
          <p className="text-3xl font-bold mt-2 text-gray-800">$13,699.00</p>
          <div className="mt-4 h-2 bg-gray-100 rounded-full overflow-hidden">
            <div className="h-full bg-green-500 w-[75%]"></div>
          </div>
        </div>
      </div>

      {/* Transaction history */}
      <div className="bg-white border border-[#AFD3E2] rounded-lg overflow-hidden">
        <div className="p-4 border-b border-[#AFD3E2] bg-gray-50">
          <h3 className="font-bold text-[#146C94]">Recent transactions</h3>
        </div>
        <table className="w-full text-left">
          <thead>
            <tr className="text-xs text-gray-400 uppercase border-b border-[#AFD3E2]">
              <th className="p-4">Txn ID</th>
              <th className="p-4">Auction</th>
              <th className="p-4">Amount</th>
              <th className="p-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50 text-sm">
            {paymentHistory.map((item) => (
              <tr key={item.id} className="hover:bg-[#F6F1F1]">
                <td className="p-4 font-mono text-xs">{item.id}</td>
                <td className="p-4 font-medium">{item.auction}</td>
                <td className="p-4 font-bold text-[#146C94]">${item.amount.toLocaleString()}</td>
                <td className="p-4">
                  <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                    item.status === 'Completed' ? 'bg-green-100 text-green-700' :
                    item.status === 'Processing' ? 'bg-blue-100 text-blue-700' : 'bg-yellow-100 text-yellow-700'
                  }`}>
                    {item.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}