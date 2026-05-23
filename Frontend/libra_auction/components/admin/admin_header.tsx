'use client';

import Image from "next/image";
import Link from "next/link";

interface AdminHeaderProps {
  title: string;
  breadcrumb?: Array<{ label: string; href?: string }>;
}

export default function AdminHeader({ title, breadcrumb }: AdminHeaderProps) {
  return (
    <div className="fixed top-0 left-64 right-0 h-16 flex items-center justify-start px-6 z-40">
    </div>
  );
}
