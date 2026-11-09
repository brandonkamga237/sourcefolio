"use client";

import { useLayoutEffect, useRef } from "react";

interface TabsProps {
  label: string;
  tabs: readonly { key: string; label: string }[];
  current: string;
  onChange: (key: string) => void;
}

/** The indicator is one pixel wide and scaled, so moving it stays on the compositor. */
export function Tabs({ label, tabs, current, onChange }: TabsProps) {
  const root = useRef<HTMLDivElement>(null);
  const indicator = useRef<HTMLSpanElement>(null);

  useLayoutEffect(() => {
    const place = () => {
      const active = root.current?.querySelector<HTMLElement>('[aria-selected="true"]');
      if (active && indicator.current) indicator.current.style.transform = `translateX(${active.offsetLeft}px) scaleX(${active.offsetWidth})`;
    };
    place();
    const observer = new ResizeObserver(place);
    if (root.current) observer.observe(root.current);
    return () => observer.disconnect();
  }, [current]);

  function move(offset: number) {
    const index = tabs.findIndex((tab) => tab.key === current);
    const next = tabs[(index + offset + tabs.length) % tabs.length];
    onChange(next.key);
    root.current?.querySelector<HTMLElement>(`[data-tab="${next.key}"]`)?.focus();
  }

  return (
    <div
      ref={root}
      role="tablist"
      aria-label={label}
      onKeyDown={(event) => {
        if (event.key === "ArrowRight") move(1);
        if (event.key === "ArrowLeft") move(-1);
      }}
      className="relative flex gap-6 overflow-x-auto border-b border-line"
    >
      {tabs.map((tab) => (
        <button
          key={tab.key}
          type="button"
          role="tab"
          data-tab={tab.key}
          aria-selected={tab.key === current}
          tabIndex={tab.key === current ? 0 : -1}
          onClick={() => onChange(tab.key)}
          className={`min-h-11 shrink-0 text-[15px] transition-colors lg:text-[16px] duration-(--dur-2) ${tab.key === current ? "font-semibold text-ink" : "text-ink-muted hover:text-ink"}`}
        >
          {tab.label}
        </button>
      ))}
      <span ref={indicator} aria-hidden="true" className="absolute bottom-0 left-0 h-0.5 w-px origin-left bg-tab-active transition-transform duration-(--dur-3) ease-enter" />
    </div>
  );
}
