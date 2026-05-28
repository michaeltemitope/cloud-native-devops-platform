package com.opsbytemitope.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
}
