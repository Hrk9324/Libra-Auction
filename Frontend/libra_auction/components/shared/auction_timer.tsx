"use client";

import { useEffect, useState, useRef } from "react";

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
  // Track accumulated pause duration
  const pausedStartRef = useRef<number | null>(null);
  const totalPausedMsRef = useRef(0);
  const prevIsPausedRef = useRef(isPaused);

  // When isPaused changes from false -> true, record pause start
  if (isPaused && !prevIsPausedRef.current) {
    pausedStartRef.current = Date.now();
  }
  // When isPaused changes from true -> false, accumulate pause duration
  if (!isPaused && prevIsPausedRef.current && pausedStartRef.current !== null) {
    totalPausedMsRef.current += Date.now() - pausedStartRef.current;
    pausedStartRef.current = null;
  }
  prevIsPausedRef.current = isPaused;

  // Adjusted end time = original endTime + total paused duration
  const adjustedEndTime = endTimeMs + totalPausedMsRef.current;

  const [timeLeftMs, setTimeLeftMs] = useState(() =>
    Math.max(0, adjustedEndTime - Date.now())
  );

  useEffect(() => {
    if (isPaused) {
      // While paused, don't tick — but show frozen remaining time
      setTimeLeftMs(Math.max(0, adjustedEndTime - Date.now()));
      return;
    }

    const interval = setInterval(() => {
      const remaining = Math.max(0, adjustedEndTime - Date.now());
      setTimeLeftMs(remaining);
      onTick?.(remaining);

      if (remaining <= 0) {
        clearInterval(interval);
        onEnd?.();
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [isPaused, adjustedEndTime, onTick, onEnd]);

  const displayMs = timeLeftMs;

  const hours = Math.floor(displayMs / (1000 * 60 * 60));
  const minutes = Math.floor((displayMs % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((displayMs % (1000 * 60)) / 1000);

  const pad = (n: number) => n.toString().padStart(2, "0");
  const timeStr = `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;

  const getColorClass = () => {
    if (isPaused) return "text-yellow-600";
    if (displayMs <= 60 * 1000) return "text-red-600";
    if (displayMs <= 5 * 60 * 1000) return "text-yellow-600";
    return "text-green-600";
  };

  const sizeClasses = {
    sm: "text-lg font-semibold",
    md: "text-2xl font-bold",
    lg: "text-4xl font-bold tracking-wider",
  };

  return (
    <div className="flex items-center justify-center gap-3">
      <div className={`${sizeClasses[size]} ${getColorClass()} font-mono`} suppressHydrationWarning>
        {isPaused ? (
          <span className="flex items-center gap-2">
            <span className="animate-pulse">⏸</span>
            <span className="line-through opacity-60">{timeStr}</span>
            <span className="text-yellow-600 text-sm font-sans ml-2">
              TẠM DỪNG
            </span>
          </span>
        ) : displayMs <= 0 ? (
          <span className="text-red-600">ĐÃ KẾT THÚC</span>
        ) : (
          timeStr
        )}
      </div>
    </div>
  );
}
