package com.onlinelearning.course.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long categoryId;
    private String name;
    private String description;
    private String iconUrl;
}
