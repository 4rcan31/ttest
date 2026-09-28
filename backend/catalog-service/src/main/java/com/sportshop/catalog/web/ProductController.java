package com.sportshop.catalog.web;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.catalog.dto.CategoryResponse;
import com.sportshop.catalog.dto.PageResponse;
import com.sportshop.catalog.dto.ProductResponse;
import com.sportshop.catalog.service.ProductSearchCriteria;
import com.sportshop.catalog.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Catálogo")
@SecurityRequirements
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Buscar artículos del inventario (paginado)")
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "featured") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return productService.search(
                new ProductSearchCriteria(query, category, minPrice, maxPrice, inStock, sort, page, size));
    }

    @Operation(summary = "Consultar el detalle de un artículo")
    @GetMapping("/{id:\\d+}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @Operation(summary = "Listar las categorías del catálogo")
    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return productService.categories();
    }
}
