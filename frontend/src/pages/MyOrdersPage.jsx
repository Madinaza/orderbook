import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { api } from "../lib/api";
import { formatDate, formatMoney, statusClass } from "../lib/format";
import ReplaceOrderModal from "../components/ReplaceOrderModal";
import OrderDetailsModal from "../components/OrderDetailsModal";
import StatCard from "../components/StatCard";
import PaginationBar from "../components/PaginationBar";

const DEFAULT_FILTERS = {
  instrument: "",
  status: "",
  side: "",
  orderType: "",
  page: 0,
  size: 10,
  sortBy: "createdAt",
  sortDirection: "desc"
};

const QUICK_INSTRUMENTS = ["AAPL", "MSFT", "NVDA", "TSLA", "AMZN"];

export default function MyOrdersPage() {
  const [ordersPage, setOrdersPage] = useState({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    sortBy: "createdAt",
    sortDirection: "desc"
  });

  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailsOrder, setDetailsOrder] = useState(null);

  const [draftFilters, setDraftFilters] = useState(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(DEFAULT_FILTERS);

  const orders = ordersPage.content || [];
  const modalOpen = Boolean(selectedOrder || detailsOrder);
  const modalOpenRef = useRef(false);

  useEffect(() => {
    modalOpenRef.current = modalOpen;
  }, [modalOpen]);

  const loadOrders = useCallback(async () => {
    if (modalOpenRef.current) {
      return;
    }

    try {
      setError("");
      const data = await api.getMyOrders(appliedFilters);

      if (modalOpenRef.current) {
        return;
      }

      setOrdersPage(data);
    } catch (err) {
      if (modalOpenRef.current) {
        return;
      }
      setError(err.message || "Failed to load orders.");
    }
  }, [appliedFilters]);

  useEffect(() => {
    if (modalOpen) {
      return;
    }

    loadOrders();
  }, [loadOrders, modalOpen]);

  useEffect(() => {
    if (modalOpen) {
      return;
    }

    const intervalId = setInterval(() => {
      loadOrders();
    }, 4000);

    return () => clearInterval(intervalId);
  }, [loadOrders, modalOpen]);

  function updateFilter(event) {
    const { name, value } = event.target;
    setDraftFilters((prev) => ({ ...prev, [name]: value }));
  }

  function applyFilters() {
    setAppliedFilters((prev) => ({
      ...prev,
      instrument: draftFilters.instrument.trim().toUpperCase(),
      status: draftFilters.status,
      side: draftFilters.side,
      orderType: draftFilters.orderType,
      sortBy: draftFilters.sortBy,
      sortDirection: draftFilters.sortDirection,
      page: 0,
      size: draftFilters.size
    }));
  }

  function resetFilters() {
    setDraftFilters(DEFAULT_FILTERS);
    setAppliedFilters(DEFAULT_FILTERS);
  }

  function applyQuickInstrument(instrument) {
    setDraftFilters((prev) => ({ ...prev, instrument }));
    setAppliedFilters((prev) => ({
      ...prev,
      instrument,
      page: 0
    }));
  }

  function changePage(nextPage) {
    if (nextPage < 0) return;
    if (ordersPage.totalPages > 0 && nextPage >= ordersPage.totalPages) return;

    setAppliedFilters((prev) => ({
      ...prev,
      page: nextPage
    }));
  }

  function changePageSize(nextSize) {
    setDraftFilters((prev) => ({ ...prev, size: nextSize }));
    setAppliedFilters((prev) => ({
      ...prev,
      size: nextSize,
      page: 0
    }));
  }

  async function cancelOrder(id) {
    try {
      setActionLoading(true);
      setError("");
      await api.cancelOrder(id);

      if (detailsOrder?.id === id) {
        setDetailsOrder(null);
      }
      if (selectedOrder?.id === id) {
        setSelectedOrder(null);
      }

      await loadOrders();
    } catch (err) {
      setError(err.message || "Cancel failed.");
    } finally {
      setActionLoading(false);
    }
  }

  async function replaceOrder(id, payload) {
    try {
      setActionLoading(true);
      setError("");
      await api.replaceOrder(id, payload);
      setSelectedOrder(null);
      await loadOrders();
    } catch (err) {
      setError(err.message || "Replace failed.");
    } finally {
      setActionLoading(false);
    }
  }

  function openDetails(order) {
    setDetailsOrder(structuredCloneSafe(order));
  }

  function openReplace(order) {
    setSelectedOrder(structuredCloneSafe(order));
  }

  function canEdit(order) {
    return (
      order.orderType === "LIMIT" &&
      (order.status === "NEW" || order.status === "PARTIALLY_FILLED")
    );
  }

  const openOrders = useMemo(
    () => orders.filter((order) => order.status === "NEW" || order.status === "PARTIALLY_FILLED").length,
    [orders]
  );

  const filledOrders = useMemo(
    () => orders.filter((order) => order.status === "FILLED").length,
    [orders]
  );

  const cancelledOrders = useMemo(
    () => orders.filter((order) => order.status === "CANCELLED").length,
    [orders]
  );

  const limitOrders = useMemo(
    () => orders.filter((order) => order.orderType === "LIMIT").length,
    [orders]
  );

  const marketOrders = useMemo(
    () => orders.filter((order) => order.orderType === "MARKET").length,
    [orders]
  );

  return (
    <>
      <div className="stats-row stats-row-4">
        <StatCard label="Total Orders" value={ordersPage.totalElements} accent="blue" compact />
        <StatCard label="Open Orders" value={openOrders} accent="yellow" compact />
        <StatCard
          label="Filled / Cancelled"
          value={`${filledOrders} / ${cancelledOrders}`}
          accent="blue"
          compact
        />
        <StatCard
          label="Limit / Market"
          value={`${limitOrders} / ${marketOrders}`}
          accent="yellow"
          compact
        />
      </div>

      <section className="panel-card fade-up orders-panel-pro">
        <div className="panel-header orders-panel-header">
          <div>
            <h2>My Orders</h2>
            <p className="panel-subtitle">
              Monitor live order status, inspect audit history, and manage active orders.
            </p>
          </div>
          <span className="mini-badge">{ordersPage.totalElements}</span>
        </div>

        <div className="orders-filter-shell">
          <div className="orders-filter-top">
            <div className="orders-filter-title-block">
              <h3>Filter Workspace</h3>
              <p>Refine your order list by instrument, status, side, order type, and sorting.</p>
            </div>
          </div>

          <div className="quick-chip-row">
            {QUICK_INSTRUMENTS.map((instrument) => (
              <button
                key={instrument}
                type="button"
                className={`quick-chip ${draftFilters.instrument === instrument ? "quick-chip-active" : ""}`}
                onClick={() => applyQuickInstrument(instrument)}
              >
                {instrument}
              </button>
            ))}
          </div>

          <div className="filters-grid orders-filters-grid">
            <label>
              <span>Instrument</span>
              <input
                name="instrument"
                value={draftFilters.instrument}
                onChange={updateFilter}
                placeholder="AAPL"
              />
            </label>

            <label>
              <span>Status</span>
              <select name="status" value={draftFilters.status} onChange={updateFilter}>
                <option value="">All</option>
                <option value="NEW">NEW</option>
                <option value="PARTIALLY_FILLED">PARTIALLY_FILLED</option>
                <option value="FILLED">FILLED</option>
                <option value="CANCELLED">CANCELLED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </label>

            <label>
              <span>Side</span>
              <select name="side" value={draftFilters.side} onChange={updateFilter}>
                <option value="">All</option>
                <option value="BUY">BUY</option>
                <option value="SELL">SELL</option>
              </select>
            </label>

            <label>
              <span>Order Type</span>
              <select name="orderType" value={draftFilters.orderType} onChange={updateFilter}>
                <option value="">All</option>
                <option value="LIMIT">LIMIT</option>
                <option value="MARKET">MARKET</option>
              </select>
            </label>

            <label>
              <span>Sort By</span>
              <select name="sortBy" value={draftFilters.sortBy} onChange={updateFilter}>
                <option value="createdAt">Created Time</option>
                <option value="updatedAt">Updated Time</option>
                <option value="status">Status</option>
                <option value="instrument">Instrument</option>
              </select>
            </label>

            <label>
              <span>Direction</span>
              <select name="sortDirection" value={draftFilters.sortDirection} onChange={updateFilter}>
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </label>
          </div>

          <div className="orders-toolbar">
            <div className="toolbar-actions orders-toolbar-actions">
              <button className="primary-button toolbar-button" onClick={applyFilters} type="button">
                Apply Filters
              </button>
              <button className="secondary-button toolbar-button" onClick={resetFilters} type="button">
                Reset
              </button>
              <button className="table-action secondary-action toolbar-button" onClick={loadOrders} type="button">
                Refresh
              </button>
            </div>
          </div>
        </div>

        {error && <div className="error-box">{error}</div>}

        <div className="orders-table-section">
          <div className="section-bar bar-blue">ORDER STATUS TABLE</div>

          <PaginationBar
            page={ordersPage.page}
            totalPages={ordersPage.totalPages}
            totalElements={ordersPage.totalElements}
            pageSize={ordersPage.size}
            onPrevious={() => changePage(Math.max(ordersPage.page - 1, 0))}
            onNext={() => changePage(ordersPage.page + 1)}
            onPageSizeChange={changePageSize}
          />

          <div className="table-wrap orders-table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Instrument</th>
                  <th>Side</th>
                  <th>Type</th>
                  <th>Price</th>
                  <th>Original Qty</th>
                  <th>Open Qty</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan="11" className="empty-cell">
                      No orders found for the selected filters
                    </td>
                  </tr>
                ) : (
                  orders.map((order) => (
                    <tr key={order.id}>
                      <td>{order.id}</td>
                      <td>{order.instrument}</td>
                      <td>{order.side}</td>
                      <td>{order.orderType}</td>
                      <td>{formatMoney(order.limitPrice)}</td>
                      <td>{order.originalQty}</td>
                      <td>{order.openQty}</td>
                      <td>
                        <span className={`status-pill ${statusClass(order.status)}`}>
                          {order.status}
                        </span>
                      </td>
                      <td>{formatDate(order.createdAt)}</td>
                      <td>{formatDate(order.updatedAt)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            className="table-action secondary-action"
                            onClick={() => openDetails(order)}
                            type="button"
                          >
                            Details
                          </button>

                          {(order.status === "NEW" || order.status === "PARTIALLY_FILLED") && (
                            <button
                              className="table-action"
                              disabled={actionLoading}
                              onClick={() => cancelOrder(order.id)}
                              type="button"
                            >
                              Cancel
                            </button>
                          )}

                          {canEdit(order) && (
                            <button
                              className="table-action secondary-action"
                              disabled={actionLoading}
                              onClick={() => openReplace(order)}
                              type="button"
                            >
                              Replace
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <ReplaceOrderModal
        open={Boolean(selectedOrder)}
        order={selectedOrder}
        loading={actionLoading}
        onClose={() => setSelectedOrder(null)}
        onSubmit={replaceOrder}
      />

      <OrderDetailsModal
        open={Boolean(detailsOrder)}
        order={detailsOrder}
        onClose={() => setDetailsOrder(null)}
      />
    </>
  );
}

function structuredCloneSafe(value) {
  if (typeof structuredClone === "function") {
    return structuredClone(value);
  }
  return JSON.parse(JSON.stringify(value));
}