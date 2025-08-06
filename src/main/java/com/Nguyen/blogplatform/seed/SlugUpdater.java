package com.Nguyen.blogplatform.seed;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.Nguyen.blogplatform.service.CategoryServices;

@Component
public class SlugUpdater implements CommandLineRunner {

    private final CategoryServices categoryService;

    public SlugUpdater(CategoryServices categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) throws Exception {
        categoryService.generateSlugsForExistingCategories();
    }
}