"use client";

import dynamic from "next/dynamic";
import { useEffect, useRef, useState, type ComponentProps } from "react";
import type { Globe as GlobeComponent } from "./Globe";

const Globe = dynamic(() => import("./Globe").then((module) => module.Globe), { ssr: false, loading: () => <div className="mx-auto aspect-square w-full max-w-[720px]" /> });

/** WebGL and its library arrive only when the visitor gets near the globe. */
export function GlobeLazy(props: ComponentProps<typeof GlobeComponent>) {
  const root = useRef<HTMLDivElement>(null);
  const [near, setNear] = useState(false);
  useEffect(() => {
    if (!root.current) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setNear(true);
        observer.disconnect();
      }
    }, { rootMargin: "400px" });
    observer.observe(root.current);
    return () => observer.disconnect();
  }, []);
  return <div ref={root}>{near ? <Globe {...props} /> : <div className="mx-auto aspect-square w-full max-w-[720px]" />}</div>;
}
