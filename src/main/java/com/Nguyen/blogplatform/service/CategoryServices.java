package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.payload.request.CategoryRequest;
import com.Nguyen.blogplatform.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServices {

    private final CategoryRepository categoryRepository;

    public CategoryServices(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public List<Category> getAllCategory(){
        return categoryRepository.findAll();
    }

    public boolean isCategoryExit(CategoryRequest category){
        Optional<Category> existingCategory = categoryRepository.findByCategory(category.getCategory());
        return existingCategory.isPresent();
    }
    public Category getCategoryById( Long id){
        return categoryRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Not found "+ id));
    }
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id){
        categoryRepository.deleteById(id);
    }

}
