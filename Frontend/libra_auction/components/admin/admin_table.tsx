'use client';

interface Column<T> {
  key: keyof T;
  label: string;
  render?: (value: any, row: T) => React.ReactNode;
  width?: string;
}

interface AdminTableProps<T> {
  columns: Column<T>[];
  data: T[];
  actions?: Array<{
    label: string;
    onClick: (row: T) => void;
    color?: "primary" | "success" | "danger" | "warning";
  }>;
  loading?: boolean;
  emptyMessage?: string;
}

export default function AdminTable<T extends Record<string, any>>({
  columns,
  data,
  actions,
  loading = false,
  emptyMessage = "No data available",
}: AdminTableProps<T>) {
  const getActionColor = (color?: string) => {
    const colors: Record<string, string> = {
      primary: "bg-[#19A7CE] hover:bg-[#1589ab]",
      success: "bg-green-500 hover:bg-green-600",
      danger: "bg-red-500 hover:bg-red-600",
      warning: "bg-amber-500 hover:bg-amber-600",
    };
    return colors[color || "primary"] || colors.primary;
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-8 text-center">
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-8 text-center">
        <p className="text-gray-500">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg border border-gray-200 shadow-sm">
      <table className="w-full">
        {/* Header */}
        <thead>
          <tr className="bg-gray-50 border-b border-gray-200">
            {columns.map((col) => (
              <th
                key={String(col.key)}
                className={`px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider ${
                  col.width || ""
                }`}
              >
                {col.label}
              </th>
            ))}
            {actions && actions.length > 0 && (
              <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                Actions
              </th>
            )}
          </tr>
        </thead>

        {/* Body */}
        <tbody>
          {data.map((row, idx) => (
            <tr
              key={idx}
              className="border-b border-gray-100 hover:bg-gray-50 transition-colors"
            >
              {columns.map((col) => (
                <td
                  key={String(col.key)}
                  className={`px-6 py-4 text-sm text-gray-800 ${col.width || ""}`}
                >
                  {col.render ? col.render(row[col.key], row) : row[col.key]}
                </td>
              ))}
              {actions && actions.length > 0 && (
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    {actions.map((action, actionIdx) => (
                      <button
                        key={actionIdx}
                        onClick={() => action.onClick(row)}
                        className={`px-3 py-1 rounded text-white text-xs font-semibold transition-colors ${getActionColor(
                          action.color
                        )}`}
                      >
                        {action.label}
                      </button>
                    ))}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
