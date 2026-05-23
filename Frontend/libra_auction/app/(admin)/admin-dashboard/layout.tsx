'use client';

import { ReactNode } from 'react';
import AdminSidebar from '@/components/admin/admin_sidebar';
import AdminHeader from '@/components/admin/admin_header';

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen bg-[#F6F1F1]">
      {/* Sidebar */}
      <AdminSidebar />

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <AdminHeader title="Dashboard" breadcrumb={[]} />

        {/* Content Area */}
        <main className="flex-1 overflow-y-auto">
          <div className="p-6">{children}</div>
        </main>
      </div>
    </div>
  );
}
