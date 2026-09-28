package com.sportshop.catalog.web;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.catalog.dto.ProductResponse;
import com.sportshop.catalog.dto.ReservedProduct;
import com.sportshop.catalog.dto.StockRequest;
import com.sportshop.catalog.service.ProductService;
import com.sportshop.catalog.service.StockService;

import io.swagger.v3.oas.annotations.Hidden;

/** API interna consumida por order-service. Protegida con API key y no publicada en el gateway. */
@Hidden
@Validated
@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    private final ProductService productService;
    private final StockService stockService;

    public InternalProductController(ProductService productService, StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    @GetMapping
    public List<ProductResponse> getByIds(@RequestParam @Size(max = 100) List<Long> ids) {
        return productService.getByIds(ids);
    }

    @PostMapping("/reserve")
    public List<ReservedProduct> reserve(@Valid @RequestBody StockRequest request) {
        return stockService.reserve(request);
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@Valid @RequestBody StockRequest request) {
        stockService.release(request);
    }
}
