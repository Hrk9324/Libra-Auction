import { ReactNode } from 'react';
import AdminSidebar from '@/components/admin/admin_sidebar';
import AdminHeader from '@/components/admin/admin_header';

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen bg-[#F6F1F1]">
      <AdminSidebar />
      <div className="flex-1 flex flex-col">
        <AdminHeader title="Admin Dashboard" breadcrumb={[]} />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
