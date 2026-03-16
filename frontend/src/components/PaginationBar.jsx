export default function PaginationBar({
  page,
  totalPages,
  totalElements,
  onPrevious,
  onNext,
  onPageSizeChange,
  pageSize
}) {
  const safeTotalPages = Math.max(totalPages || 0, 1);
  const safePage = Math.max(page || 0, 0);

  return (
    <div className="pagination-bar">
      <div className="pagination-summary">
        <span>Total: {totalElements ?? 0}</span>
        <span>
          Page {safePage + 1} / {safeTotalPages}
        </span>
      </div>

      <div className="pagination-actions">
        <label className="pagination-size">
          <span>Rows</span>
          <select value={pageSize} onChange={(e) => onPageSizeChange(Number(e.target.value))}>
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
        </label>

        <button
          type="button"
          className="table-action secondary-action"
          onClick={onPrevious}
          disabled={safePage === 0}
        >
          Previous
        </button>

        <button
          type="button"
          className="table-action secondary-action"
          onClick={onNext}
          disabled={safePage + 1 >= safeTotalPages}
        >
          Next
        </button>
      </div>
    </div>
  );
}