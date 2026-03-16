package com.aghaz.orderbook.shared_contracts.api;

import java.util.List;

/**
 * Standard paginated API response for list endpoints.
 * Keeps pagination contract consistent across services.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        String sortBy,
        String sortDirection
) {
}