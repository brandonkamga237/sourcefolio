"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";

// One client per browser tab, created once: server pages fetch their own data, the browser uses
// TanStack Query for what changes under the visitor's hands (likes).
export function QueryProvider({ children }: { children: ReactNode }) {
  const [client] = useState(() => new QueryClient({ defaultOptions: { queries: { staleTime: 30_000, retry: 1 } } }));
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
