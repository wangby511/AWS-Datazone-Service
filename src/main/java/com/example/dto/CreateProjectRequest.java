package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String description;
}
