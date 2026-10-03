package ru.yandex.practicum.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.service.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAll() {
        List<CategoryDto> categories = categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();

        log.debug("Получено категорий: {}", categories.size());

        return categories;
    }

    @Override
    public CategoryDto getById(Long id) {
        log.debug("Поиск категории по id={}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));

        log.debug("Категория найдена: id={}, name={}", category.getId(), category.getName());

        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        log.debug("Начало создания категории name={}", request.name());

        Category category = CategoryMapper.toEntity(request);

        log.debug("Категория сформирована, выполняется сохранение");

        Category saved = categoryRepository.save(category);

        log.debug("Категория сохранена с id={}", saved.getId());

        return CategoryMapper.toDto(saved);
    }
}