package com.example.model;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class PaginatedResult<T> {
    private List<T> items;
    private String nextToken;
}
