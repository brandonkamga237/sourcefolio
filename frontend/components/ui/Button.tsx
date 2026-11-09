import type { ButtonHTMLAttributes, ReactNode } from "react";
import { IconCheck } from "@/components/icons";
import { SquareSpinner } from "./SquareSpinner";

export type ButtonStatus = "idle" | "busy" | "done";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  tone?: "primary" | "secondary";
  status?: ButtonStatus;
  /** Shown after success: the action's own name in the past, "Publish" then "Published". */
  doneLabel?: ReactNode;
  children: ReactNode;
}

const layer = "col-start-1 row-start-1 flex items-center justify-center gap-2 transition-[opacity,transform] duration-(--dur-2) ease-enter";
const shown = "opacity-100";
const hidden = "opacity-0 scale-90";

/** All three states occupy the same cell, so the button never changes width while it works. */
export function Button({ tone = "primary", status = "idle", doneLabel, children, className = "", disabled, ...rest }: ButtonProps) {
  return (
    <button {...rest} disabled={disabled || status === "busy"} aria-busy={status === "busy"} className={`btn ${tone === "primary" ? "btn-primary" : "btn-secondary"} ${className}`}>
      <span className="grid">
        <span className={`${layer} ${status === "idle" ? shown : hidden}`} aria-hidden={status !== "idle"}>{children}</span>
        <span className={`${layer} ${status === "busy" ? shown : hidden}`} aria-hidden="true"><SquareSpinner /></span>
        <span className={`${layer} ${status === "done" ? shown : hidden}`} aria-hidden={status !== "done"}>
          <IconCheck aria-hidden="true" className="size-4" />
          {doneLabel ?? children}
        </span>
      </span>
    </button>
  );
}
