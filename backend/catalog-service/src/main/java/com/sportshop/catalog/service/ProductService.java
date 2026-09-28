package com.sportshop.catalog.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sportshop.catalog.domain.Category;
import com.sportshop.catalog.domain.Product;
import com.sportshop.catalog.dto.CategoryResponse;
import com.sportshop.catalog.dto.PageResponse;
import com.sportshop.catalog.dto.ProductResponse;
import com.sportshop.catalog.repository.CategoryRepository;
import com.sportshop.catalog.repository.ProductRepository;
import com.sportshop.common.web.ApiException;

@Service
@Transactional(readOnly = true)
public class ProductService {

    static final int MAX_PAGE_SIZE = 48;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResponse<ProductResponse> search(ProductSearchCriteria criteria) {
        int size = Math.clamp(criteria.size(), 1, MAX_PAGE_SIZE);
        int page = Math.max(criteria.page(), 0);
        PageRequest pageable = PageRequest.of(page, size, toSort(criteria.sort()));
        return PageResponse.from(productRepository.findAll(toSpecification(criteria), pageable),
                ProductResponse::from);
    }

    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .map(ProductResponse::from)
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "El artículo no existe"));
    }

    public List<ProductResponse> getByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productRepository.findByIdInAndActiveTrue(ids).stream().map(ProductResponse::from).toList();
    }

    public List<CategoryResponse> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    private static Specification<Product> toSpecification(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            Join<Product, Category> category = root.join("category");

            if (StringUtils.hasText(criteria.query())) {
                String pattern = "%" + escapeLike(criteria.query().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, '\\'),
                        cb.like(cb.lower(root.get("description")), pattern, '\\'),
                        cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                        cb.like(cb.lower(category.get("name")), pattern, '\\')));
            }
            if (StringUtils.hasText(criteria.category())) {
                predicates.add(cb.equal(category.get("slug"), criteria.category()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }
            if (Boolean.TRUE.equals(criteria.inStock())) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Sort toSort(String sort) {
        if (sort == null) {
            return Sort.by("id");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending().and(Sort.by("id"));
            case "price_desc" -> Sort.by("price").descending().and(Sort.by("id"));
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("id");
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
