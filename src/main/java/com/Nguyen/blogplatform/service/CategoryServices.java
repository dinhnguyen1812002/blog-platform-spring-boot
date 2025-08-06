package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Utils.SlugUtil;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.payload.request.CategoryRequest;
import com.Nguyen.blogplatform.repository.CategoryRepository;

import jakarta.transaction.Transactional;

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
    @Transactional
    public void generateSlugsForExistingCategories() {
        System.out.println("Bắt đầu cập nhật slug cho các category cũ...");
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            // Chỉ cập nhật nếu slug chưa có
            if (category.getSlug() == null || category.getSlug().isEmpty()) {
                String newSlug = SlugUtil.createSlug(category.getCategory());
                category.setSlug(newSlug);
                System.out.println("Đã tạo slug: '" + newSlug + "' cho category: '" + category.getCategory() + "'");
                // Lưu lại đối tượng đã được cập nhật
                categoryRepository.save(category);
            }
        }
        System.out.println("Hoàn tất cập nhật slug cho các category.");
    }
}
