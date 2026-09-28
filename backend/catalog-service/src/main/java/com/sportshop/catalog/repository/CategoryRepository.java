package com.sportshop.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportshop.catalog.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNameAsc();
}
