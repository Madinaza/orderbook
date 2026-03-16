export function formatMoney(value) {
  if (value === null || value === undefined || value === "") return "—";
  return Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6
  });
}

export function formatDate(value) {
  if (!value) return "—";
  try {
    return new Date(value).toLocaleString();
  } catch {
    return value;
  }
}

export function statusClass(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized.includes("filled")) return "status-filled";
  if (normalized.includes("cancel")) return "status-cancelled";
  if (normalized.includes("new")) return "status-new";
  return "status-partial";
}