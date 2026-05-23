'use client';

import { ReactNode } from "react";

interface AdminModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: "small" | "medium" | "large";
}

export default function AdminModal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = "medium",
}: AdminModalProps) {
  if (!isOpen) return null;

  const sizeClasses = {
    small: "w-96",
    medium: "w-2xl",
    large: "w-4xl",
  };

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 z-50 transition-opacity"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="fixed inset-0 flex items-center justify-center z-50 p-4">
        <div
          className={`bg-white rounded-lg shadow-2xl ${sizeClasses[size]} max-h-[90vh] overflow-y-auto`}
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h3 className="text-xl font-bold text-[#146C94]">{title}</h3>
            <button
              onClick={onClose}
              className="text-gray-500 hover:text-gray-700 transition text-2xl leading-none"
            >
              ×
            </button>
          </div>

          {/* Body */}
          <div className="p-6">{children}</div>

          {/* Footer */}
          {footer && <div className="border-t border-gray-200 p-6 bg-gray-50">{footer}</div>}
        </div>
      </div>
    </>
  );
}
