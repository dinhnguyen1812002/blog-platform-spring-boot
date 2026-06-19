package com.Nguyen.blogplatform.infrastructure.seed;




import com.Nguyen.blogplatform.domain.post.application.CategoryServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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