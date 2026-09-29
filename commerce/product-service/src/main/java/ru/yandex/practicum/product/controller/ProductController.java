package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        log.info("Получение всех товаров");
        return productService.getAllActive();
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id) {
        log.info("Получение товара по id");
        return productService.getById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductDto> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("Получение всех товаров по категории");
        return productService.getByCategory(categoryId);
    }

    @GetMapping("/search")
    public List<ProductDto> searchProducts(@RequestParam String query) {
        log.info("Поиск товаров");
        return productService.search(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Создание товара");
        return productService.create(request);
    }

    @PatchMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        log.info("Обновление товара");
        return productService.update(id, request);
    }
}