export default function StatCard({ label, value, accent = "blue", compact = false }) {
  return (
    <div className={`stat-card stat-${accent} ${compact ? "stat-compact" : ""}`}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value ?? "—"}</div>
    </div>
  );
}