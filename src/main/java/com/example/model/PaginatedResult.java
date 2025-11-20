package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResult<T> {
    private List<T> items;
    private String nextToken;
}
