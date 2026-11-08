"use client";

import dynamic from "next/dynamic";

// Its own chunk, fetched once the page is up: the map data never weighs on the first paint.
export const AfricaFieldLazy = dynamic(() => import("./AfricaField").then((module) => module.AfricaField), {
  ssr: false,
  loading: () => <div className="aspect-[72/74] w-full" />,
});
