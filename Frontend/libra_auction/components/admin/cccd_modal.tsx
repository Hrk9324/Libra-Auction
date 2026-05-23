'use client';

import Image from "next/image";
import AdminModal from "./admin_modal";

interface CCCDModalProps {
  isOpen: boolean;
  onClose: () => void;
  cccdFront: string;
  cccdBack: string;
  userName: string;
}

export default function CCCDModal({
  isOpen,
  onClose,
  cccdFront,
  cccdBack,
  userName,
}: CCCDModalProps) {
  return (
    <AdminModal
      isOpen={isOpen}
      onClose={onClose}
      title={`CCCD - ${userName}`}
      size="medium"
      footer={
        <div className="flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-300 text-gray-800 rounded font-semibold hover:bg-gray-400 transition"
          >
            Close
          </button>
        </div>
      }
    >
      <div className="space-y-6">
        {/* Mặt trước */}
        <div>
          <p className="text-sm font-semibold text-gray-600 mb-2">Front Side</p>
          <div className="bg-gray-100 rounded-lg overflow-hidden border border-gray-300">
            <Image
              src={cccdFront}
              alt="CCCD Front"
              width={400}
              height={250}
              className="w-full h-auto"
            />
          </div>
        </div>

        {/* Mặt sau */}
        <div>
          <p className="text-sm font-semibold text-gray-600 mb-2">Back Side</p>
          <div className="bg-gray-100 rounded-lg overflow-hidden border border-gray-300">
            <Image
              src={cccdBack}
              alt="CCCD Back"
              width={400}
              height={250}
              className="w-full h-auto"
            />
          </div>
        </div>
      </div>
    </AdminModal>
  );
}
