"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef } from "react";
import { localizedPath } from "@/i18n/paths";
import type { Locale } from "@/i18n/routing";
import type { FieldCity } from "@/lib/map";
import { landSquares, MAP_COLUMNS, MAP_ROWS } from "@/lib/map";

interface AfricaFieldProps {
  locale: Locale;
  cities: readonly FieldCity[];
  label: string;
  className?: string;
}

const LEVELS = [0, 1, 2, 3, 4] as const;
const FRAME_MS = 33;

/**
 * The continent as thousands of contribution squares: it builds from the south on arrival, then
 * stays alive, squares lighting and fading as a calendar fills, brighter around the pointer. A
 * canvas with one path per shade, because thousands of animated elements would stall a phone.
 * Cities with published profiles hold their shade; the light elsewhere stands for no one.
 */
export function AfricaField({ locale, cities, label, className }: AfricaFieldProps) {
  const ref = useRef<HTMLCanvasElement>(null);
  const router = useRouter();

  useEffect(() => {
    const canvas = ref.current;
    const context = canvas?.getContext("2d");
    if (!canvas || !context) return;
    const squares = landSquares();
    const energy = new Float32Array(squares.length);
    const held = new Uint8Array(squares.length);
    const byKey = new Map(squares.map(([column, row], index) => [`${column}:${row}`, index]));
    for (const city of cities) {
      const index = byKey.get(`${city.column}:${city.row}`);
      if (index !== undefined) held[index] = city.level;
    }
    const reach = squares.map(([column, row]) => Math.hypot(column - MAP_COLUMNS / 2, row - MAP_ROWS));
    const farthest = Math.max(...reach);
    const calm = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    let cell = 0;
    let colours: string[] = [];
    let progress = calm ? 1 : 0;
    let pointer: { x: number; y: number } | null = null;
    let visible = true;
    let last = 0;
    let frames = 0;
    let stop = () => {};

    const measure = () => {
      const ratio = Math.min(window.devicePixelRatio || 1, 2);
      const width = canvas.clientWidth;
      canvas.width = width * ratio;
      canvas.height = (width * MAP_ROWS * ratio) / MAP_COLUMNS;
      context.setTransform(ratio, 0, 0, ratio, 0, 0);
      cell = width / MAP_COLUMNS;
    };
    const readColours = () => {
      const style = getComputedStyle(canvas);
      colours = LEVELS.map((level) => style.getPropertyValue(`--color-level-${level}`).trim());
    };
    const draw = () => {
      const size = Math.max(1, cell * 0.8);
      const width = canvas.clientWidth;
      context.clearRect(0, 0, width, width * (MAP_ROWS / MAP_COLUMNS));
      const reached = progress * (farthest + 8);
      for (const level of LEVELS) {
        context.beginPath();
        squares.forEach(([column, row], index) => {
          if (reach[index] > reached) return;
          const shade = Math.max(held[index], Math.ceil(energy[index] * 4));
          if (shade === level) context.rect(column * cell, row * cell, size, size);
        });
        context.fillStyle = colours[level];
        context.fill();
      }
    };
    const step = (time: number) => {
      if (!visible || document.hidden || time - last < FRAME_MS) return;
      last = time;
      if (frames++ % 90 === 0) readColours();
      for (let index = 0; index < energy.length; index++) energy[index] *= 0.955;
      for (let spawn = 0; spawn < 7; spawn++) energy[Math.floor(Math.random() * energy.length)] = 0.45 + Math.random() * 0.55;
      if (pointer) {
        const { x, y } = pointer;
        squares.forEach(([column, row], index) => {
          const distance = Math.hypot(column - x, row - y);
          if (distance < 6) energy[index] = Math.max(energy[index], 1 - distance / 6);
        });
      }
      draw();
    };

    measure();
    readColours();
    draw();
    const resize = new ResizeObserver(() => {
      measure();
      draw();
    });
    resize.observe(canvas);
    const seen = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting;
    });
    seen.observe(canvas);

    if (!calm) {
      let cancelled = false;
      void import("gsap").then(({ gsap }) => {
        if (cancelled) return;
        const tween = gsap.to({ value: 0 }, { value: 1, duration: 2.4, ease: "power2.out", onUpdate() { progress = this.targets()[0].value; } });
        const tick = () => step(performance.now());
        gsap.ticker.add(tick);
        stop = () => {
          tween.kill();
          gsap.ticker.remove(tick);
        };
      });
      const previous = stop;
      stop = () => {
        cancelled = true;
        previous();
      };
    }

    const cellAt = (event: PointerEvent) => {
      const box = canvas.getBoundingClientRect();
      return { x: (event.clientX - box.left) / cell, y: (event.clientY - box.top) / cell };
    };
    const cityAt = (at: { x: number; y: number }) => cities.find((city) => Math.abs(city.column + 0.4 - at.x) < 1.2 && Math.abs(city.row + 0.4 - at.y) < 1.2);
    const move = (event: PointerEvent) => {
      if (event.pointerType === "touch") return;
      pointer = cellAt(event);
      canvas.style.cursor = cityAt(pointer) ? "pointer" : "";
    };
    const leave = () => {
      pointer = null;
    };
    const open = (event: PointerEvent) => {
      if (event.pointerType === "touch") return;
      const city = cityAt(cellAt(event));
      if (city) router.push(localizedPath(locale, `/rankings?country=${city.country}`));
    };
    canvas.addEventListener("pointermove", move);
    canvas.addEventListener("pointerleave", leave);
    canvas.addEventListener("click", open as EventListener);

    return () => {
      stop();
      resize.disconnect();
      seen.disconnect();
      canvas.removeEventListener("pointermove", move);
      canvas.removeEventListener("pointerleave", leave);
      canvas.removeEventListener("click", open as EventListener);
    };
  }, [cities, locale, router]);

  return <canvas ref={ref} role="img" aria-label={label} className={`block aspect-[72/74] w-full ${className ?? ""}`} />;
}
