package com.microcommerce.products.service;

import com.microcommerce.products.dto.request.CreateCategoryRequest;
import com.microcommerce.products.dto.response.CategoryResponse;
import com.microcommerce.products.entity.Category;
import com.microcommerce.products.exception.CategoryNotFoundException;
import com.microcommerce.products.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ===== CRUD Operations =====

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Création d'une nouvelle catégorie: {}", request.getName());

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .active(request.getActive())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Catégorie créée avec succès: ID={}, nom={}", savedCategory.getId(), savedCategory.getName());
        
        return convertToResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return convertToResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        log.info("Mise à jour de la catégorie ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        Category parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Une catégorie ne peut pas être son propre parent");
            }
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParent(parent);
        category.setActive(request.getActive());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Catégorie mise à jour avec succès: ID={}", updatedCategory.getId());
        
        return convertToResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {
        log.info("Suppression de la catégorie ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        List<Category> children = categoryRepository.findByParentIdAndActiveTrue(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie qui a des sous-catégories");
        }

        long productCount = categoryRepository.countProductsInCategory(id);
        if (productCount > 0) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie qui contient des produits");
        }

        categoryRepository.delete(category);
        log.info("Catégorie supprimée avec succès: ID={}", id);
    }


    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(int page, int size, String sort) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return categoryRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrue(parentId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryHierarchy(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return categoryRepository.findCategoryHierarchy(categoryId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String name, int page, int size, String sort) {
        // Parser le paramètre de tri
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::convertToResponse);
    }


    @Transactional(readOnly = true)
    public long getProductCountInCategory(Long categoryId) {
        return categoryRepository.countProductsInCategory(categoryId);
    }

    @Transactional(readOnly = true)
    public long getSubCategoryCount(Long categoryId) {
        return categoryRepository.countByParentIdAndActiveTrue(categoryId);
    }


    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by("id").ascending();
        }
        
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            
            if (!isValidSortProperty(property)) {
                property = "id";
            }
            
            return "desc".equalsIgnoreCase(direction) 
                ? Sort.by(property).descending() 
                : Sort.by(property).ascending();
        } catch (Exception e) {
            log.warn("Erreur lors du parsing du tri '{}', utilisation du tri par défaut", sort);
            return Sort.by("id").ascending();
        }
    }
    
    private boolean isValidSortProperty(String property) {
        return List.of("id", "name", "description", "createdAt", "updatedAt").contains(property);
    }

    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
