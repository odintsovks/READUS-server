package com.readus.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Cursor-paginated page: {@code {items, next_cursor}} (null cursor = last page). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> {
    private List<T> items;
    private String nextCursor;
}
