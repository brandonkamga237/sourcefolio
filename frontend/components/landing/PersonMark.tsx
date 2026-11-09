import { Avatar } from "@/components/members/Avatar";

interface PersonMarkProps {
  login: string;
  size: number;
  /** A fictional login must never fetch an avatar: it could belong to someone real. */
  example: boolean;
  name?: string | null;
}

export function PersonMark({ login, size, example, name }: PersonMarkProps) {
  if (!example) return <Avatar login={login} size={size} />;
  return (
    <span aria-hidden="true" style={{ width: size, height: size, fontSize: Math.round(size * 0.38) }} className="flex shrink-0 items-center justify-center rounded-full bg-level-1 font-semibold text-level-4">
      {(name ?? login).charAt(0).toUpperCase()}
    </span>
  );
}
