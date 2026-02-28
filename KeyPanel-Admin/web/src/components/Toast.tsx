export function Toast({ message }: { message: string }) {
  if (!message) return null;
  return <div className="fixed top-4 right-4 glass px-4 py-2 text-neon z-50">{message}</div>;
}
