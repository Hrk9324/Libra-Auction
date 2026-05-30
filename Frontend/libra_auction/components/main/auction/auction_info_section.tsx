'use client';
import { useState, useEffect } from 'react';
import Image from "next/image";
import Link from "next/link";
import { Auction } from "@/types/auction/auction";
import { CurrencyFormat } from '@/utils/currency_format';
import { TimeFormat } from '@/utils/time_format';
import { DurationFormat } from '@/utils/duration_format';
import countdown from '@/public/countdown.png';
import startFlag from '@/public/start_flag.png';
import increment from '@/public/increment.png';
import calendar from '@/public/calendar.png';
import hourGlass from '@/public/hourglass.png';
import clock from '@/public/clock.png';

export default function AuctionInfoSection({
  autionInfos
}: {
  autionInfos: Auction
}) {
  const [activeImage, setActiveImage] = useState(autionInfos.images[0]);
  const [isRegistering, setIsRegistering] = useState(false);
  const [timeLeft, setTimeLeft] = useState({ days: 0, hours: 0, minutes: 0, seconds: 0 });
  const isLive = autionInfos.auction_status === "IN_PROGRESS";
  const startDate = new Date(autionInfos.start_time);

  const formatDateOnly = (date: Date) => {
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date);
  };

  const formatTimeOnly = (date: Date) => {
    return new Intl.DateTimeFormat('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).format(date);
  };

  // Countdown Timer Logic
  useEffect(() => {
    const calculateTimeLeft = () => {
      const difference = new Date(autionInfos.start_time).getTime() - new Date().getTime();
      if (difference > 0) {
        setTimeLeft({
          days: Math.floor(difference / (1000 * 60 * 60 * 24)),
          hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
          minutes: Math.floor((difference / 1000 / 60) % 60),
          seconds: Math.floor((difference / 1000) % 60),
        });
      }
    };

    calculateTimeLeft();
    const timer = setInterval(calculateTimeLeft, 1000);
    return () => clearInterval(timer);
  }, [autionInfos.start_time]);

  const handleRegister = async () => {
    setIsRegistering(true);
    // try {
    //   await fetch("/auctions-register", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({ auction_id: autionInfos.auction_id })
    //   });
    //   // Handle success locally
    // } catch (error) {
    //   console.error("Registration failed", error);
    // } finally {
    //   setIsRegistering(false);
    // }
  };

  return (
    <div className="min-h-screen bg-gray-50/50 pt-10 px-16">
      <div className="mx-auto">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 p-8 lg:p-12">
            <div className="flex flex-col gap-6">
              <div className="relative aspect-square w-full rounded-2xl bg-gray-50 border border-gray-100 overflow-hidden shadow-sm group flex items-center justify-center">
                <Image
                  src={activeImage}
                  alt={autionInfos.product_name}
                  fill
                  className="object-contain p-4 transition-transform duration-500 group-hover:scale-105"
                />
              </div>
              <div className="flex gap-4 overflow-x-auto pb-2 scrollbar-hide">
                {autionInfos.images.map((img, index) => (
                  <button
                    key={index}
                    onClick={() => setActiveImage(img)}
                    className={`relative shrink-0 size-24 rounded-2xl overflow-hidden border-2 transition-all duration-200 
                      ${activeImage === img ? 'border-(--primary-color)' : 'border-gray-200 hover:border-(--primary-color)/60 opacity-70 hover:opacity-100'}`}
                  >
                    <Image
                      src={img}
                      fill
                      alt={`${autionInfos.product_name} thumbnail ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </button>
                ))}
              </div>
            </div>
            <div className="flex flex-col justify-between h-full">
              <div className="space-y-8">
                <div className="space-y-4">
                  <div className="flex items-center gap-2 text-sm text-gray-500 font-medium uppercase tracking-wider">
                    <span>{autionInfos.category_name}</span>
                    <span>#{autionInfos.product_id}</span>
                  </div>
                  <h1 className="text-4xl font-bold text-gray-900 leading-tight line-clamp-2">
                    {autionInfos.product_name}
                  </h1>
                </div>
                <div className="bg-(--primary-color)/15 border border-(--primary-color)/5 rounded-2xl p-6 relative overflow-hidden">
                  <h3 className="text-(--secondary-color) font-semibold mb-4 flex items-center gap-2">
                    <Image src={countdown} width={20} height={20} alt='' />
                    {isLive ? 'Auction Live Now' : 'Auction Starts In'}
                  </h3>
                  <div className="flex gap-4 md:gap-6 text-center relative justify-center">
                    {Object.entries(timeLeft).map(([unit, value]) => (
                      <div key={unit} className="flex flex-col items-center bg-white rounded-xl shadow-sm px-4 py-3 min-w-17.5 md:min-w-22.5">
                        <span className="text-3xl md:text-4xl font-bold text-(--secondary-color)">
                          {value.toString().padStart(2, '0')}
                        </span>
                        <span className="text-xs md:text-sm text-gray-500 uppercase tracking-wider font-medium mt-1">
                          {unit}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={startFlag} width={16} height={16} alt='' />
                      Starting Price
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                      {CurrencyFormat(autionInfos.starting_price)}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={startFlag} width={16} height={16} alt='' />
                      Deposit
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                      {CurrencyFormat(autionInfos.deposit_amount)}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={increment} width={16} height={16} alt='' />
                      Min. Increment
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                      {CurrencyFormat(autionInfos.min_bid_increment)}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={calendar} width={16} height={16} alt='' />
                      Start Date
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                      {formatDateOnly(startDate)}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={clock} width={16} height={16} alt='' />
                      Start Time
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                      {formatTimeOnly(startDate)}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-500 mb-2">
                      <Image src={hourGlass} width={16} height={16} alt='' />
                      Duration
                    </div>
                    <div className="text-2xl font-bold text-gray-900">
                        {DurationFormat(autionInfos.duration * 1000)}
                      </div>
                  </div>
                </div>
              </div>
              <div className="pt-4 mt-4 border-t border-gray-100">
                {isLive ? (
                  <Link
                    href={`/auctions/${autionInfos.category_id}/${autionInfos.auction_id}/live`}
                    className="w-full flex items-center justify-center gap-3 bg-green-600 hover:bg-green-700 active:scale-[0.98] text-white text-lg font-bold py-5 px-8 rounded-2xl shadow-lg transition-all duration-200 group"
                  >
                    View live
                  </Link>
                ) : (
                  <>
                    <button
                      onClick={handleRegister}
                      disabled={isRegistering}
                      className="w-full flex items-center justify-center gap-3 bg-(--secondary-color)/90 hover:bg-(--secondary-color)/80 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed text-white text-lg font-bold py-5 px-8 rounded-2xl shadow-lg transition-all duration-200 group"
                    >
                      {isRegistering ? (
                        <div className="animate-pulse">Loading...</div>
                      ) : (
                        <>Register to Join Auction</>
                      )}
                    </button>
                    <p className="text-center text-sm text-gray-400 mt-4">
                      Registration is required to participate. No charges apply until you win.
                    </p>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}