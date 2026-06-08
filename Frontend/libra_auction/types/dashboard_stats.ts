import type { AccountStatus, AuctionStatus } from "@/types/status";

export type DashboardApprovalStatus = "PENDING_APPROVAL" | "PENDING" | "APPROVED" | "REJECTED";

export type DashboardRevenuePoint = {
  date: string;
  revenue: number;
};

export type DashboardAuctionSummary = {
  auctionId: string;
  productName: string;
  auctionStatus: AuctionStatus;
  approvalStatus: DashboardApprovalStatus;
  startTime: string | null;
  completedAt: string | null;
  currentPrice: number;
  totalBids: number;
  totalParticipants: number;
  winningPrice: number | null;
};

export type SellerDashboardStats = {
  totalAuctions: number;
  pendingApprovalAuctions: number;
  approvedAuctions: number;
  rejectedAuctions: number;
  liveAuctions: number;
  upcomingAuctions: number;
  completedAuctions: number;
  failedAuctions: number;
  totalBids: number;
  totalParticipants: number;
  totalRevenue: number;
  revenueLast7Days: DashboardRevenuePoint[];
  recentAuctions: DashboardAuctionSummary[];
};

export type DashboardStatusCount<T extends string> = {
  status: T;
  count: number;
};

export type AdminDashboardStats = SellerDashboardStats & {
  totalUsers: number;
  pendingUsers: number;
  activeUsers: number;
  lockedUsers: number;
  auctionStatusBreakdown: DashboardStatusCount<AuctionStatus>[];
  approvalStatusBreakdown: DashboardStatusCount<DashboardApprovalStatus>[];
  userStatusBreakdown: DashboardStatusCount<AccountStatus>[];
};
