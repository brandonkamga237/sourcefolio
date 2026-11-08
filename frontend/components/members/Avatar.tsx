"use client";

import Image from "next/image";
import { useState } from "react";

/** GitHub serves every account's public avatar at this address; no token, no extra scope. */
function avatarUrl(login: string, size: number): string {
  return `https://github.com/${encodeURIComponent(login)}.png?size=${size}`;
}

interface AvatarProps {
  login: string;
  /** Rendered size in pixels; the image is fetched at twice that for sharp screens. */
  size: number;
  className?: string;
}

// The member's GitHub avatar; their initials when GitHub cannot be reached, so a card never
// shows a broken image.
export function Avatar({ login, size, className = "" }: AvatarProps) {
  const [failed, setFailed] = useState(false);
  if (failed) {
    return (
      <span
        aria-hidden="true"
        style={{ width: size, height: size, fontSize: Math.max(10, Math.round(size * 0.36)) }}
        className={`flex shrink-0 items-center justify-center rounded-full bg-accent-soft font-semibold text-accent-ink uppercase ${className}`}
      >
        {login.slice(0, 2)}
      </span>
    );
  }
  return (
    <Image
      src={avatarUrl(login, size * 2)}
      alt=""
      width={size}
      height={size}
      unoptimized
      onError={() => setFailed(true)}
      style={{ width: size, height: size }}
      className={`shrink-0 rounded-full bg-accent-soft object-cover ${className}`}
    />
  );
}
