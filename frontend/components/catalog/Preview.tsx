"use client";

import Image from "next/image";
import { useRef } from "react";
import { PreviewArt } from "./PreviewArt";

interface PreviewProps {
  slug: string;
  siteUrl: string;
  imageUrl: string | null;
  videoUrl?: string | null;
  size?: "thumb" | "card" | "large";
  aspectClass?: string;
  /** Hint for next/image, so a phone downloads a phone-sized file. */
  sizes: string;
  priority?: boolean;
}

// The author's preview: their image, and their short video played on hover where a mouse can
// hover (spec: never autoplayed on a phone, where it would cost data). Without an image, the
// site's own name stands in (PreviewArt).
export function Preview({ slug, siteUrl, imageUrl, videoUrl, size = "card", aspectClass = "aspect-[16/10]", sizes, priority }: PreviewProps) {
  const video = useRef<HTMLVideoElement>(null);
  if (!imageUrl) {
    return <PreviewArt slug={slug} siteUrl={siteUrl} size={size} aspectClass={aspectClass} />;
  }

  const canHover = () => window.matchMedia("(hover: hover) and (pointer: fine)").matches;
  return (
    <div
      className={`relative overflow-hidden bg-surface-sunken ${aspectClass}`}
      onMouseEnter={() => {
        if (videoUrl && canHover()) void video.current?.play().catch(() => undefined);
      }}
      onMouseLeave={() => video.current?.pause()}
    >
      <Image src={imageUrl} alt="" fill sizes={sizes} priority={priority} className="object-cover object-top transition-transform duration-500 ease-out-soft group-hover:scale-[1.04]" />
      {videoUrl && size !== "thumb" ? (
        <video
          ref={video}
          src={videoUrl}
          poster={imageUrl}
          muted
          loop
          playsInline
          preload="none"
          aria-hidden="true"
          className="absolute inset-0 size-full object-cover object-top"
        />
      ) : null}
    </div>
  );
}
