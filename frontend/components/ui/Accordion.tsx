"use client";

import * as Primitive from "@radix-ui/react-accordion";
import { IconChevronRight } from "@/components/icons";

export function Accordion({ items }: { items: readonly { key: string; question: string; answer: string }[] }) {
  return (
    <Primitive.Root type="single" collapsible className="divide-y divide-line border-y border-line">
      {items.map((item) => (
        <Primitive.Item key={item.key} value={item.key}>
          <Primitive.Header>
            <Primitive.Trigger className="group flex min-h-14 w-full items-center justify-between gap-4 py-3 text-left text-[16px] font-medium">
              {item.question}
              <IconChevronRight aria-hidden="true" className="size-4 shrink-0 text-ink-muted transition-transform duration-(--dur-2) ease-enter group-data-[state=open]:rotate-90" />
            </Primitive.Trigger>
          </Primitive.Header>
          <Primitive.Content className="overflow-hidden data-[state=closed]:animate-[accordion-close_var(--dur-2)_var(--ease-exit)] data-[state=open]:animate-[accordion-open_var(--dur-3)_var(--ease-enter)]">
            <p className="max-w-2xl pb-4 text-[15px] leading-relaxed text-ink-soft">{item.answer}</p>
          </Primitive.Content>
        </Primitive.Item>
      ))}
    </Primitive.Root>
  );
}
