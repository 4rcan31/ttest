package com.sportshop.catalog.dto;

import com.sportshop.catalog.domain.Category;

public record CategoryResponse(String slug, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getSlug(), category.getName());
    }
}
