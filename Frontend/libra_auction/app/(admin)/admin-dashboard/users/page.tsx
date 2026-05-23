'use client';

import { useState, useEffect } from "react";
import Image from "next/image";
import AdminTable from "@/components/admin/admin_table";
import CCCDModal from "@/components/admin/cccd_modal";

interface UserData {
  id: string;
  avatar: string;
  name: string;
  email: string;
  phone: string;
  cccd: string;
  cccdFront: string;
  cccdBack: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt: string;
}

// Mock Users Data
const mockUsers: UserData[] = [
  {
    id: "1",
    avatar: "/default-avatar.png",
    name: "Nguyễn Văn A",
    email: "nguyenvana@example.com",
    phone: "0901234567",
    cccd: "123456789",
    cccdFront: "/default-avatar.png",
    cccdBack: "/default-avatar.png",
    status: "PENDING",
    createdAt: "2026-05-10",
  },
  {
    id: "2",
    avatar: "/default-avatar.png",
    name: "Trần Thị B",
    email: "tranthib@example.com",
    phone: "0912345678",
    cccd: "987654321",
    cccdFront: "/default-avatar.png",
    cccdBack: "/default-avatar.png",
    status: "PENDING",
    createdAt: "2026-05-12",
  },
  {
    id: "3",
    avatar: "/default-avatar.png",
    name: "Lê Minh C",
    email: "leminch@example.com",
    phone: "0923456789",
    cccd: "456123789",
    cccdFront: "/default-avatar.png",
    cccdBack: "/default-avatar.png",
    status: "APPROVED",
    createdAt: "2026-05-08",
  },
  {
    id: "4",
    avatar: "/default-avatar.png",
    name: "Phạm Văn D",
    email: "phamvand@example.com",
    phone: "0934567890",
    cccd: "789456123",
    cccdFront: "/default-avatar.png",
    cccdBack: "/default-avatar.png",
    status: "REJECTED",
    createdAt: "2026-05-05",
  },
];

export default function UsersApprovalPage() {
  // Store list of users awaiting approval
  const [users, setUsers] = useState<UserData[]>(mockUsers);
  // Store CCCD document info for modal display
  const [selectedCCCD, setSelectedCCCD] = useState<{
    isOpen: boolean;
    front: string;
    back: string;
    name: string;
  }>({ isOpen: false, front: "", back: "", name: "" });
  // Store selected status filter
  const [statusFilter, setStatusFilter] = useState<"ALL" | "PENDING" | "APPROVED" | "REJECTED">("ALL");

  // Fetch pending users from backend on component mount
  useEffect(() => {
    const fetchPendingUsers = async () => {
      try {
        const response = await fetch('/api/admin/users/pending');
        if (response.ok) {
          const data = await response.json();
          setUsers(data);
        } else {
          console.error('Failed to fetch pending users:', response.statusText);
        }
      } catch (error) {
        console.error('Error fetching pending users:', error);
      }
    };
    fetchPendingUsers();
  }, []);

  // Send approval request to backend and update local state
  const handleApprove = async (user: UserData) => {
    try {
      const response = await fetch(`/api/admin/users/${user.id}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.ok) {
        // Update user status to APPROVED in UI
        setUsers(
          users.map((u) =>
            u.id === user.id ? { ...u, status: "APPROVED" } : u
          )
        );
        console.log("User approved:", user.id);
      } else {
        console.error('Failed to approve user:', response.statusText);
      }
    } catch (error) {
      console.error('Error approving user:', error);
    }
  };

  // Send rejection request to backend and update local state
  const handleReject = async (user: UserData) => {
    try {
      const response = await fetch(`/api/admin/users/${user.id}/reject`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.ok) {
        // Update user status to REJECTED in UI
        setUsers(
          users.map((u) =>
            u.id === user.id ? { ...u, status: "REJECTED" } : u
          )
        );
        console.log("User rejected:", user.id);
      } else {
        console.error('Failed to reject user:', response.statusText);
      }
    } catch (error) {
      console.error('Error rejecting user:', error);
    }
  };

  const handleViewCCCD = (user: UserData) => {
    setSelectedCCCD({
      isOpen: true,
      front: user.cccdFront,
      back: user.cccdBack,
      name: user.name,
    });
  };

  const getStatusBadge = (status: string) => {
    const statusStyles = {
      PENDING: "bg-amber-100 text-amber-800 border border-amber-300",
      APPROVED: "bg-green-100 text-green-800 border border-green-300",
      REJECTED: "bg-red-100 text-red-800 border border-red-300",
    };
    const labels = {
      PENDING: "Pending",
      APPROVED: "Approved",
      REJECTED: "Rejected",
    };
    return (
      <span
        className={`px-3 py-1 rounded-full text-xs font-semibold ${
          statusStyles[status as keyof typeof statusStyles]
        }`}
      >
        {labels[status as keyof typeof labels]}
      </span>
    );
  };

  const pendingCount = users.filter((u) => u.status === "PENDING").length;
  const approvedCount = users.filter((u) => u.status === "APPROVED").length;
  const rejectedCount = users.filter((u) => u.status === "REJECTED").length;

  // Filter users based on selected status
  const filteredUsers = statusFilter === "ALL" 
    ? users 
    : users.filter((u) => u.status === statusFilter);

  const tableColumns = [
    {
      key: "avatar" as const,
      label: "Avatar",
      render: (value: string, row: UserData) => (
        <Image
          src={row.avatar}
          alt={row.name}
          width={40}
          height={40}
          className="w-10 h-10 rounded-full"
          onError={(e) => {
            e.currentTarget.src = '/default-avatar.png';
          }}
        />
      ),
      width: "w-16",
    },
    {
      key: "name" as const,
      label: "Full Name",
      render: (value: string) => <span className="font-medium">{value}</span>,
    },
    {
      key: "email" as const,
      label: "Email",
      render: (value: string) => <span className="text-sm">{value}</span>,
    },
    {
      key: "phone" as const,
      label: "Phone",
      render: (value: string) => <span className="text-sm">{value}</span>,
    },
    {
      key: "cccd" as const,
      label: "CCCD",
      render: (value: string, row: UserData) => (
        <button
          onClick={() => handleViewCCCD(row)}
          className="text-[#19A7CE] font-semibold hover:underline"
        >
          {value}
        </button>
      ),
    },
    {
      key: "status" as const,
      label: "Status",
      render: (value: string) => getStatusBadge(value),
    },
    {
      key: "createdAt" as const,
      label: "Created At",
      render: (value: string) => <span className="text-sm">{value}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Pending</p>
          <p className="text-2xl font-bold text-amber-600 mt-1">{pendingCount}</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Approved</p>
          <p className="text-2xl font-bold text-green-600 mt-1">{approvedCount}</p>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200">
          <p className="text-xs font-semibold text-gray-600 uppercase">Rejected</p>
          <p className="text-2xl font-bold text-red-600 mt-1">{rejectedCount}</p>
        </div>
      </div>

      {/* Filter by Status */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-bold text-[#146C94] mb-4">Filter by Status</h3>
        <div className="flex gap-3 flex-wrap">
          <button
            onClick={() => setStatusFilter("ALL")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "ALL"
                ? "bg-[#19A7CE] text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            All
          </button>
          <button
            onClick={() => setStatusFilter("PENDING")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "PENDING"
                ? "bg-amber-500 text-white"
                : "bg-amber-100 text-amber-800 hover:bg-amber-200"
            }`}
          >
            Pending ({pendingCount})
          </button>
          <button
            onClick={() => setStatusFilter("APPROVED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "APPROVED"
                ? "bg-green-600 text-white"
                : "bg-green-100 text-green-800 hover:bg-green-200"
            }`}
          >
            Approved ({approvedCount})
          </button>
          <button
            onClick={() => setStatusFilter("REJECTED")}
            className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
              statusFilter === "REJECTED"
                ? "bg-red-600 text-white"
                : "bg-red-100 text-red-800 hover:bg-red-200"
            }`}
          >
            Rejected ({rejectedCount})
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200 bg-gray-50">
                {tableColumns.map((col) => (
                  <th
                    key={String(col.key)}
                    className="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    {col.label}
                  </th>
                ))}
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((row, rowIndex) => (
                <tr key={rowIndex} className="border-b border-gray-100 hover:bg-gray-50">
                  {tableColumns.map((col) => (
                    <td key={String(col.key)} className="px-6 py-4 text-sm text-gray-700">
                      {col.render ? col.render(row[col.key], row) : String(row[col.key])}
                    </td>
                  ))}
                  <td className="px-6 py-4 text-sm">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleApprove(row)}
                        className="px-3 py-1 bg-green-100 text-green-700 rounded hover:bg-green-200 text-xs font-semibold"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleReject(row)}
                        className="px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 text-xs font-semibold"
                      >
                        Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* CCCD Modal */}
      <CCCDModal
        isOpen={selectedCCCD.isOpen}
        onClose={() => setSelectedCCCD({ ...selectedCCCD, isOpen: false })}
        cccdFront={selectedCCCD.front}
        cccdBack={selectedCCCD.back}
        userName={selectedCCCD.name}
      />
    </div>
  );
}
