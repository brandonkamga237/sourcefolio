"use client";

import createGlobe from "cobe";
import Link from "next/link";
import { useEffect, useRef, useState, type CSSProperties, type ReactNode } from "react";
import { PersonMark } from "@/components/landing/PersonMark";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";

export interface GlobeCity {
  id: string;
  latitude: number;
  longitude: number;
  label: string;
  count: string;
  size: number;
  country: string;
  login: string | null;
}

interface GlobeProps {
  locale: Locale;
  cities: GlobeCity[];
  example: boolean;
  label: string;
  open: string;
  /** The flat map, when WebGL is missing or motion is reduced. */
  fallback: ReactNode;
}

const AFRICA = Math.PI - ((20 * Math.PI) / 180 - Math.PI / 2);
const anglesOf = (city: GlobeCity) => [Math.PI - ((city.longitude * Math.PI) / 180 - Math.PI / 2), (city.latitude * Math.PI) / 180] as const;

function canDraw(): boolean {
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return false;
  try {
    return Boolean(document.createElement("canvas").getContext("webgl2") ?? document.createElement("canvas").getContext("webgl"));
  } catch {
    return false;
  }
}

function isDark(): boolean {
  const theme = document.documentElement.dataset.theme;
  return theme ? theme === "dark" : window.matchMedia("(prefers-color-scheme: dark)").matches;
}

export function Globe({ locale, cities, example, label, open, fallback }: GlobeProps) {
  const canvas = useRef<HTMLCanvasElement>(null);
  const [drawable] = useState(canDraw);
  const [selected, setSelected] = useState<string | null>(null);
  const [hovered, setHovered] = useState<string | null>(null);
  const [themeRun, setThemeRun] = useState(0);
  const motion = useRef({ phi: AFRICA, theta: 0.12, scale: 1, targetPhi: null as number | null, targetTheta: 0.12, targetScale: 1, dragging: false, lastX: 0 });

  useEffect(() => {
    const element = canvas.current;
    if (!drawable || !element) return;
    const dark = isDark();
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    let width = element.offsetWidth;
    const state = motion.current;
    const globe = createGlobe(element, {
      devicePixelRatio: ratio,
      width: width * ratio,
      height: width * ratio,
      phi: state.phi,
      theta: state.theta,
      dark: dark ? 1 : 0,
      diffuse: dark ? 1.4 : 1.1,
      mapSamples: 16000,
      mapBrightness: dark ? 5 : 1.6,
      baseColor: dark ? [0.16, 0.2, 0.26] : [0.95, 0.96, 0.97],
      markerColor: [0.18, 0.64, 0.31],
      glowColor: dark ? [0.08, 0.11, 0.16] : [0.93, 0.95, 0.97],
      markers: cities.map((city) => ({ location: [city.latitude, city.longitude] as [number, number], size: city.size, id: city.id })),
    });
    let frame = 0;
    let visible = true;
    const tick = () => {
      if (visible && !document.hidden) {
        if (state.targetPhi !== null) {
          state.phi += (state.targetPhi - state.phi) * 0.08;
          state.theta += (state.targetTheta - state.theta) * 0.08;
        } else if (!state.dragging) {
          state.phi += 0.0022;
        }
        state.scale += (state.targetScale - state.scale) * 0.08;
        globe.update({ phi: state.phi, theta: state.theta, scale: state.scale });
      }
      frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    const seen = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting;
    });
    seen.observe(element);
    const resize = new ResizeObserver(() => {
      if (element.offsetWidth === width) return;
      width = element.offsetWidth;
      globe.update({ width: width * ratio, height: width * ratio });
    });
    resize.observe(element);
    return () => {
      cancelAnimationFrame(frame);
      seen.disconnect();
      resize.disconnect();
      globe.destroy();
    };
  }, [drawable, cities, themeRun]);

  useEffect(() => {
    const again = () => setThemeRun((value) => value + 1);
    const attribute = new MutationObserver(again);
    attribute.observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });
    const media = window.matchMedia("(prefers-color-scheme: dark)");
    media.addEventListener("change", again);
    return () => {
      attribute.disconnect();
      media.removeEventListener("change", again);
    };
  }, []);

  if (!drawable) return <>{fallback}</>;

  function focus(city: GlobeCity) {
    const state = motion.current;
    const [phi, theta] = anglesOf(city);
    // The nearest equivalent angle, so the globe never spins the long way round.
    const turns = Math.round((state.phi - phi) / (2 * Math.PI));
    state.targetPhi = phi + turns * 2 * Math.PI;
    state.targetTheta = theta;
    state.targetScale = 1.25;
    setSelected(city.id);
  }

  const shown = cities.find((city) => city.id === (hovered ?? selected));
  const anchored = (city: GlobeCity): CSSProperties => ({ positionAnchor: `--cobe-${city.id}`, top: "anchor(center)", left: "anchor(center)", translate: "-50% -50%", opacity: `var(--cobe-visible-${city.id}, 0)` }) as CSSProperties;

  return (
    <div className="relative mx-auto aspect-square w-full max-w-[640px]">
      <canvas
        ref={canvas}
        role="img"
        aria-label={label}
        style={{ touchAction: "pan-y" }}
        className="size-full cursor-grab active:cursor-grabbing"
        onPointerDown={(event) => {
          const state = motion.current;
          state.dragging = true;
          state.lastX = event.clientX;
          state.targetPhi = null;
          state.targetScale = 1;
          setSelected(null);
          event.currentTarget.setPointerCapture(event.pointerId);
        }}
        onPointerMove={(event) => {
          const state = motion.current;
          if (!state.dragging) return;
          state.phi += (event.clientX - state.lastX) / 180;
          state.lastX = event.clientX;
        }}
        onPointerUp={() => { motion.current.dragging = false; }}
        onPointerCancel={() => { motion.current.dragging = false; }}
      />
      {cities.map((city) => (
        <button key={city.id} type="button" aria-label={`${city.label} · ${city.count}`} onClick={() => focus(city)} onPointerEnter={() => setHovered(city.id)} onPointerLeave={() => setHovered(null)} onFocus={() => setHovered(city.id)} onBlur={() => setHovered(null)} style={anchored(city)} className="globe-marker absolute flex size-11 items-center justify-center">
          <span aria-hidden="true" className="size-2 rounded-[2px] bg-level-3" />
          <span aria-hidden="true" className="absolute size-2 rounded-[2px] bg-level-3" style={{ animation: "marker-pulse 2.4s var(--ease-exit) infinite" }} />
        </button>
      ))}
      {shown ? (
        <div style={{ positionAnchor: `--cobe-${shown.id}`, bottom: "anchor(top)", left: "anchor(center)", translate: "-50% -10px" } as CSSProperties} className="globe-marker absolute z-10 flex w-max max-w-60 items-center gap-2.5 rounded-card border border-line bg-surface p-2.5 shadow-[var(--shadow-float)]">
          {shown.login ? <PersonMark login={shown.login} size={28} example={example} /> : null}
          <span className="flex min-w-0 flex-col">
            <span className="truncate text-[14px] font-semibold">{shown.label}</span>
            <span className="text-[12px] text-ink-muted">{shown.count}</span>
            {selected === shown.id && !example ? <Link href={localizedPath(locale, `/rankings?country=${shown.country}`)} className="text-[12px] font-semibold text-accent">{open}</Link> : null}
          </span>
        </div>
      ) : null}
    </div>
  );
}
