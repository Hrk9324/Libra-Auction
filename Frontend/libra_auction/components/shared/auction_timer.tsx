"use client";

import { useEffect, useState } from "react";

interface AuctionTimerProps {
  endTimeMs: number;
  isPaused: boolean;
  onTick?: (timeLeftMs: number) => void;
  onEnd?: () => void;
  size?: "sm" | "md" | "lg";
}

export default function AuctionTimer({
  endTimeMs,
  isPaused,
  onTick,
  onEnd,
  size = "md",
}: AuctionTimerProps) {
  const [timeLeftMs, setTimeLeftMs] = useState(() =>
    isPaused ? 0 : Math.max(0, endTimeMs - Date.now())
  );

  // Reset timer immediately when endTimeMs or isPaused changes
  useEffect(() => {
    if (isPaused) {
      // When paused, don't update timeLeftMs - keep it frozen
      return;
    }
    // Calculate remaining from the server-provided endTimeMs
    const remaining = Math.max(0, endTimeMs - Date.now());
    setTimeLeftMs(remaining);
  }, [endTimeMs, isPaused]);

  // Tick every second when not paused
  useEffect(() => {
    if (isPaused) return;

    const interval = setInterval(() => {
      const remaining = Math.max(0, endTimeMs - Date.now());
      setTimeLeftMs(remaining);
      onTick?.(remaining);

      if (remaining <= 0) {
        clearInterval(interval);
        onEnd?.();
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [isPaused, endTimeMs, onTick, onEnd]);

  const sizeClasses = {
    sm: "text-lg font-semibold",
    md: "text-2xl font-bold",
    lg: "text-5xl font-bold tracking-[0.18em]",
  };

  if (isPaused) {
    return (
      <div className="rounded-2xl border border-amber-200 bg-amber-50 px-6 py-5 text-center">
        <div className={`${sizeClasses[size]} text-amber-700 font-mono`}>
          <span className="font-sans tracking-[0.22em]">TẠM DỪNG</span>
        </div>
        <p className="mt-2 text-sm text-amber-700">Phiên đấu giá đang giữ thời gian hiện tại</p>
      </div>
    );
  }

  const hours = Math.floor(timeLeftMs / (1000 * 60 * 60));
  const minutes = Math.floor((timeLeftMs % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((timeLeftMs % (1000 * 60)) / 1000);
  const pad = (n: number) => n.toString().padStart(2, "0");
  const timeStr = `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;

  const getColorClass = () => {
    if (timeLeftMs <= 60 * 1000) return "text-red-600";
    if (timeLeftMs <= 5 * 60 * 1000) return "text-yellow-600";
    return "text-emerald-600";
  };

  return (
    <div className="rounded-2xl border border-[#EAF3F6] bg-[#F8FCFD] px-6 py-5 text-center shadow-inner shadow-white/60">
      <div className={`${sizeClasses[size]} ${getColorClass()} font-mono`} suppressHydrationWarning>
        {timeLeftMs <= 0 ? (
          <span className="text-rose-600">ĐÃ KẾT THÚC</span>
        ) : (
          timeStr
        )}
      </div>
      <p className="mt-2 text-sm text-[#5A7184]">
        {timeLeftMs <= 0
          ? "Phiên đấu giá đã kết thúc"
          : "Đếm ngược theo thời gian thực"}
      </p>
    </div>
  );
}
