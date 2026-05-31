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

  // Reset timer when endTimeMs changes (server sent newEndTime after resume)
  useEffect(() => {
    if (!isPaused) {
      setTimeLeftMs(Math.max(0, endTimeMs - Date.now()));
    }
  }, [endTimeMs, isPaused]);

  useEffect(() => {
    if (isPaused) {
      // Don't tick when paused - freeze display
      return;
    }

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
    lg: "text-4xl font-bold tracking-wider",
  };

  if (isPaused) {
    return (
      <div className="flex items-center justify-center gap-3">
        <div className={`${sizeClasses[size]} text-yellow-600 font-mono`}>
          <span className="flex items-center gap-2">
            <span className="animate-pulse">⏸</span>
            <span className="text-yellow-600 font-sans">TẠM DỪNG</span>
          </span>
        </div>
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
    return "text-green-600";
  };

  return (
    <div className="flex items-center justify-center gap-3">
      <div className={`${sizeClasses[size]} ${getColorClass()} font-mono`} suppressHydrationWarning>
        {timeLeftMs <= 0 ? (
          <span className="text-red-600">ĐÃ KẾT THÚC</span>
        ) : (
          timeStr
        )}
      </div>
    </div>
  );
}
