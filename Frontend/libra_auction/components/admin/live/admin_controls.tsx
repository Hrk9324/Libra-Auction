"use client";

import { useState } from "react";

interface AdminControlsProps {
  auctionId: string;
  currentStatus: string;
  onPause: () => void;
  onResume: () => void;
  onEnd: () => void;
  onCancel: (reason: string) => void;
  onSendNotification: (message: string) => void;
  isLoading: boolean;
}

export default function AdminControls({
  currentStatus,
  onPause,
  onResume,
  onEnd,
  onCancel,
  onSendNotification,
  isLoading,
}: AdminControlsProps) {
  const [notificationMessage, setNotificationMessage] = useState("");
  const [showConfirm, setShowConfirm] = useState<"end" | "cancel" | null>(
    null
  );
  const [cancelReason, setCancelReason] = useState("");

  const isEnded =
    currentStatus === "ENDED" || currentStatus === "CANCELLED";
  const isPaused = currentStatus === "PAUSED";
  const isLive = currentStatus === "IN_PROGRESS";
  const isNotStarted = currentStatus === "NOT_STARTED";

  const primaryButtonClass =
    "w-full rounded-xl px-4 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50";

  const handleSendNotification = () => {
    if (notificationMessage.trim()) {
      onSendNotification(notificationMessage.trim());
      setNotificationMessage("");
    }
  };

  const handleConfirmAction = () => {
    if (showConfirm === "end") {
      onEnd();
    } else if (showConfirm === "cancel") {
      onCancel(cancelReason.trim());
      setCancelReason("");
    }
    setShowConfirm(null);
  };

  return (
    <div className="rounded-2xl border border-[#AFD3E2] bg-white p-6 shadow-sm shadow-[#AFD3E2]/20">
      <h3 className="mb-4 text-lg font-bold text-[#146C94]">
        Điều khiển phiên đấu giá
      </h3>

      {/* Confirmation Dialog */}
      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/35 backdrop-blur-sm">
          <div className="mx-4 max-w-md rounded-2xl border border-[#AFD3E2] bg-white p-6 shadow-2xl">
            <h4 className="mb-2 text-lg font-bold text-[#146C94]">
              {showConfirm === "end"
                ? "Kết thúc phiên đấu giá?"
                : "Hủy phiên đấu giá?"}
            </h4>
            <p className="mb-4 text-sm text-[#5A7184]">
              {showConfirm === "end"
                ? "Hành động này sẽ kết thúc phiên đấu giá và xác định người thắng. Không thể hoàn tác."
                : "Phiên đấu giá chưa bắt đầu sẽ bị hủy. Sản phẩm sẽ về trạng thái sẵn sàng."}
            </p>
            {showConfirm === "cancel" && (
              <div className="mb-4">
                <label className="mb-1 block text-sm font-medium text-[#146C94]">
                  Lý do hủy <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="Nhập lý do hủy phiên đấu giá..."
                  rows={3}
                  className="w-full resize-none rounded-xl border border-[#AFD3E2] px-3 py-2 text-sm outline-none focus:border-[#19A7CE] focus:ring-2 focus:ring-[#19A7CE]/20"
                />
              </div>
            )}
            <div className="flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowConfirm(null);
                  setCancelReason("");
                }}
                className="rounded-xl border border-[#AFD3E2] px-4 py-2 text-sm font-semibold text-[#5A7184] transition hover:bg-[#F6FBFC]"
              >
                Hủy
              </button>
              <button
                onClick={handleConfirmAction}
                disabled={showConfirm === "cancel" && !cancelReason.trim()}
                className="rounded-xl bg-[#146C94] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[#0d5a7a] disabled:cursor-not-allowed disabled:opacity-50"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Control Buttons */}
      <div className="space-y-3 mb-6">
        {/* Pause / Resume */}
        {isLive && (
          <button
            onClick={onPause}
            disabled={isLoading}
            className={`${primaryButtonClass} bg-amber-500 text-white hover:bg-amber-600`}
          >
            Tạm dừng phiên
          </button>
        )}

        {isPaused && (
          <button
            onClick={onResume}
            disabled={isLoading}
            className={`${primaryButtonClass} bg-emerald-600 text-white hover:bg-emerald-700`}
          >
            Tiếp tục phiên
          </button>
        )}

        {/* End Auction */}
        {!isEnded && (
          <button
            onClick={() => setShowConfirm("end")}
            disabled={isLoading}
            className={`${primaryButtonClass} bg-rose-600 text-white hover:bg-rose-700`}
          >
            Kết thúc phiên
          </button>
        )}

        {/* Cancel Auction - only when NOT_STARTED */}
        {isNotStarted && (
          <button
            onClick={() => setShowConfirm("cancel")}
            disabled={isLoading}
            className={`${primaryButtonClass} border border-rose-300 bg-white text-rose-700 hover:bg-rose-50`}
          >
            Hủy phiên
          </button>
        )}

        {isEnded && (
          <div className="rounded-xl border border-[#EAF3F6] bg-[#F8FCFD] p-4 text-center">
            <p className="text-sm text-[#5A7184]">
              Phiên đấu giá đã{" "}
              {currentStatus === "ENDED" ? "kết thúc" : "bị hủy"}
            </p>
          </div>
        )}
      </div>

      {/* Send Notification */}
      <div className="border-t border-[#AFD3E2] pt-4">
        <h4 className="mb-2 text-sm font-semibold text-[#146C94]">
          Gửi thông báo
        </h4>
        <div className="flex gap-2">
          <input
            type="text"
            value={notificationMessage}
            onChange={(e) => setNotificationMessage(e.target.value)}
            placeholder="Nhập thông báo..."
            className="flex-1 rounded-xl border border-[#AFD3E2] px-3 py-2 text-sm outline-none focus:border-[#19A7CE] focus:ring-2 focus:ring-[#19A7CE]/20"
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSendNotification();
            }}
          />
          <button
            onClick={handleSendNotification}
            disabled={!notificationMessage.trim()}
            className="rounded-xl bg-[#146C94] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[#0d5a7a] disabled:cursor-not-allowed disabled:opacity-50"
          >
            Gửi
          </button>
        </div>
      </div>
    </div>
  );
}
