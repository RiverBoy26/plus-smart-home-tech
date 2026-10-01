package ru.yandex.practicum.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
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
                .map(ProductMapper::toDto)
                .toList();

        log.debug("Получено активных товаров: {}", products.size());

        return products;
    }

    @Override
    public ProductDto getById(Long id) {
        log.debug("Поиск товара по id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар с id=" + id + " не найден"));

        log.debug("Товар найден: id={}, name={}", product.getId(), product.getName());

        return ProductMapper.toDto(product);
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
                        .map(ProductMapper::toDto)
                        .toList();

        log.debug("Для категории id={} найдено товаров: {}", categoryId, products.size());

        return products;
    }

    @Override
    public List<ProductDto> search(String query) {
        log.debug("Поиск активных товаров");
        return productRepository.findAllByNameContainingIgnoreCaseAndActiveTrue(query)
                .stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductDto create(CreateProductRequest request) {
        log.debug("Начало создания товара name={}", request.name());

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Категория с id="
                                            + request.categoryId() + " не найдена"));
        }

        Product product = ProductMapper.toEntity(request, category);

        log.debug("Товар сформирован, categoryId={}", request.categoryId());

        Product saved = productRepository.save(product);

        log.debug("Товар сохранён с id={}", saved.getId());

        return ProductMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductDto update(
            Long id,
            UpdateProductRequest request
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар с id=" + id + " не найден"));

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Категория с id="
                                            + request.categoryId() + " не найдена"));
        }

        ProductMapper.updateEntity(product, request, category);

        Product saved = productRepository.save(product);

        log.debug("Товар id={} сохранён после обновления", saved.getId());

        return ProductMapper.toDto(saved);
    }
}