package ru.yandex.practicum.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductDto> getAllActive() {
        List<ProductDto> products = productRepository.findAllByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Получено активных товаров: {}", products.size());

        return products;
    }

    @Override
    public ProductDto getById(Long id) {
        log.debug("Поиск товара по id={}", id);

        Product product = findProduct(id);

        log.debug("Товар найден: id={}, name={}", product.getId(), product.getName());

        return toDto(product);
    }

    @Override
    public List<ProductDto> getByCategory(Long categoryId) {
        log.debug("Получение товаров категории id={}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            log.debug("Категория id={} не найдена", categoryId);

            throw new NotFoundException("Категория с id=" + categoryId + " не найдена");
        }

        List<ProductDto> products =
                productRepository.findAllByCategoryIdAndActiveTrue(categoryId)
                        .stream()
                        .map(this::toDto)
                        .toList();

        log.debug("Для категории id={} найдено товаров: {}", categoryId, products.size());

        return products;
    }

    @Override
    public List<ProductDto> search(String query) {
        log.debug("Поиск активных товаров");
        return productRepository.findAllByNameContainingIgnoreCaseAndActiveTrue(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductDto create(CreateProductRequest request) {
        log.debug("Начало создания товара name={}", request.name());

        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setActive(true);

        if (request.categoryId() != null) {
            product.setCategory(findCategory(request.categoryId()));
        }

        log.debug(
                "Товар сформирован, categoryId={}",
                request.categoryId()
        );

        Product saved = productRepository.save(product);

        log.debug("Товар сохранён с id={}", saved.getId());

        return toDto(saved);
    }

    @Override
    @Transactional
    public ProductDto update(
            Long id,
            UpdateProductRequest request
    ) {
        Product product = findProduct(id);

        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.categoryId() != null) {
            product.setCategory(findCategory(request.categoryId()));
        }

        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        if (request.active() != null) {
            product.setActive(request.active());
        }

        Product saved = productRepository.save(product);

        log.debug("Товар id={} сохранён после обновления", saved.getId());

        return toDto(saved);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Товар с id=" + id + " не найден"));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Категория с id=" + id + " не найдена"));
    }

    private ProductDto toDto(Product product) {
        Category category = product.getCategory();

        CategoryDto categoryDto = category == null
                ? null
                : new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                categoryDto,
                product.getImageUrl(),
                product.getActive()
        );
    }
}