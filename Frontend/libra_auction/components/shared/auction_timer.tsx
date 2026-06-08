"use client";

import { useEffect, useState } from "react";

interface AuctionTimerProps {
  endTimeMs: number;
  remainingTimeMs?: number | null;
  isPaused: boolean;
  onTick?: (timeLeftMs: number) => void;
  onEnd?: () => void;
  size?: "sm" | "md" | "lg";
}

export default function AuctionTimer({
  endTimeMs,
  remainingTimeMs,
  isPaused,
  onTick,
  onEnd,
  size = "md",
}: AuctionTimerProps) {
  const [timeLeftMs, setTimeLeftMs] = useState(() =>
    isPaused && remainingTimeMs !== undefined && remainingTimeMs !== null
      ? Math.max(0, remainingTimeMs)
      : Math.max(0, endTimeMs - Date.now())
  );

  // Tick every second when not paused
  useEffect(() => {
    if (isPaused) return;

    const updateTimeLeft = () => {
      const remaining = Math.max(0, endTimeMs - Date.now());
      setTimeLeftMs(remaining);
      onTick?.(remaining);

      if (remaining <= 0) {
        onEnd?.();
      }
    };

    const timeout = setTimeout(updateTimeLeft, 0);
    const interval = setInterval(updateTimeLeft, 1000);

    return () => {
      clearTimeout(timeout);
      clearInterval(interval);
    };
  }, [isPaused, endTimeMs, onTick, onEnd]);

  const sizeClasses = {
    sm: "text-lg font-semibold",
    md: "text-2xl font-bold",
    lg: "text-5xl font-bold tracking-[0.18em]",
  };

  const displayTimeLeftMs = isPaused && remainingTimeMs !== undefined && remainingTimeMs !== null
    ? Math.max(0, remainingTimeMs)
    : timeLeftMs;

  const hours = Math.floor(displayTimeLeftMs / (1000 * 60 * 60));
  const minutes = Math.floor((displayTimeLeftMs % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((displayTimeLeftMs % (1000 * 60)) / 1000);
  const pad = (n: number) => n.toString().padStart(2, "0");
  const timeStr = `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;

  if (isPaused) {
    return (
      <div className="rounded-2xl border border-amber-200 bg-amber-50 px-6 py-5 text-center">
        <div className={`${sizeClasses[size]} text-amber-700 font-mono`} suppressHydrationWarning>
          {timeStr}
        </div>
        <p className="mt-2 text-sm text-amber-700">PAUSED - The current time is frozen while the auction is paused</p>
      </div>
    );
  }

  const getColorClass = () => {
    if (displayTimeLeftMs <= 60 * 1000) return "text-red-600";
    if (displayTimeLeftMs <= 5 * 60 * 1000) return "text-yellow-600";
    return "text-emerald-600";
  };

  return (
    <div className="px-6 py-5 text-center">
      <div className={`${sizeClasses[size]} ${getColorClass()} font-mono`} suppressHydrationWarning>
        {displayTimeLeftMs <= 0 ? (
            <span className="text-rose-600">ENDED</span>
        ) : (
          timeStr
        )}
      </div>
      <p className="mt-2 text-sm text-[#5A7184]">
        {displayTimeLeftMs <= 0
          ? "The auction has ended"
          : "Real-time countdown"}
      </p>
    </div>
  );
}
