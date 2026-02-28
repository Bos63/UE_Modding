export function Skeleton({ rows = 4 }: { rows?: number }) {
  return (
    <div className="space-y-2">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-10 bg-white/10 rounded-xl animate-pulse" />
      ))}
    </div>
  );
}
