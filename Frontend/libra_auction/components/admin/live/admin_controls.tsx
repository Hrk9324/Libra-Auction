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
    <div className="bg-white rounded-xl border border-[#AFD3E2] p-6 shadow-sm">
      <h3 className="text-lg font-bold text-[#146C94] mb-4">
        Điều khiển phiên đấu giá
      </h3>

      {/* Confirmation Dialog */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 max-w-md mx-4 shadow-2xl">
            <h4 className="text-lg font-bold text-[#146C94] mb-2">
              {showConfirm === "end"
                ? "Kết thúc phiên đấu giá?"
                : "Hủy phiên đấu giá?"}
            </h4>
            <p className="text-sm text-[#5A7184] mb-4">
              {showConfirm === "end"
                ? "Hành động này sẽ kết thúc phiên đấu giá và xác định người thắng. Không thể hoàn tác."
                : "Phiên đấu giá chưa bắt đầu sẽ bị hủy. Sản phẩm sẽ về trạng thái sẵn sàng."}
            </p>
            {showConfirm === "cancel" && (
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Lý do hủy <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="Nhập lý do hủy phiên đấu giá..."
                  rows={3}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-red-300 focus:border-red-400 resize-none"
                />
              </div>
            )}
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => { setShowConfirm(null); setCancelReason(""); }}
                className="px-4 py-2 text-sm text-[#5A7184] border border-gray-300 rounded-lg hover:bg-gray-50 transition"
              >
                Hủy
              </button>
              <button
                onClick={handleConfirmAction}
                disabled={showConfirm === "cancel" && !cancelReason.trim()}
                className="px-4 py-2 text-sm text-white rounded-lg transition bg-red-600 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
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
            className="w-full px-4 py-3 bg-yellow-500 text-white rounded-lg font-semibold hover:bg-yellow-600 transition disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {isLoading ? (
              <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full" />
            ) : (
              "⏸"
            )}
            Tạm dừng phiên
          </button>
        )}

        {isPaused && (
          <button
            onClick={onResume}
            disabled={isLoading}
            className="w-full px-4 py-3 bg-green-600 text-white rounded-lg font-semibold hover:bg-green-700 transition disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {isLoading ? (
              <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full" />
            ) : (
              "▶"
            )}
            Tiếp tục phiên
          </button>
        )}

        {/* End Auction */}
        {!isEnded && (
          <button
            onClick={() => setShowConfirm("end")}
            disabled={isLoading}
            className="w-full px-4 py-3 bg-red-600 text-white rounded-lg font-semibold hover:bg-red-700 transition disabled:opacity-50 flex items-center justify-center gap-2"
          >
            ⏹ Kết thúc phiên
          </button>
        )}

        {/* Cancel Auction - only when NOT_STARTED */}
        {isNotStarted && (
          <button
            onClick={() => setShowConfirm("cancel")}
            disabled={isLoading}
            className="w-full px-4 py-3 border-2 border-red-500 text-red-600 rounded-lg font-semibold hover:bg-red-50 transition disabled:opacity-50 flex items-center justify-center gap-2"
          >
            ✕ Hủy phiên
          </button>
        )}

        {isEnded && (
          <div className="bg-gray-50 rounded-lg p-4 text-center">
            <p className="text-sm text-[#5A7184]">
              Phiên đấu giá đã{" "}
              {currentStatus === "ENDED" ? "kết thúc" : "bị hủy"}
            </p>
          </div>
        )}
      </div>

      {/* Send Notification */}
      <div className="border-t border-[#AFD3E2] pt-4">
        <h4 className="text-sm font-semibold text-[#146C94] mb-2">
          Gửi thông báo
        </h4>
        <div className="flex gap-2">
          <input
            type="text"
            value={notificationMessage}
            onChange={(e) => setNotificationMessage(e.target.value)}
            placeholder="Nhập thông báo..."
            className="flex-1 px-3 py-2 text-sm border border-[#AFD3E2] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#19A7CE]"
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSendNotification();
            }}
          />
          <button
            onClick={handleSendNotification}
            disabled={!notificationMessage.trim()}
            className="px-4 py-2 bg-[#146C94] text-white rounded-lg text-sm hover:bg-[#0d5a7a] transition disabled:opacity-50"
          >
            Gửi
          </button>
        </div>
      </div>
    </div>
  );
}
