package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.service.CategoryServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final CategoryServices  categoryServices;

    public CategoryController(CategoryServices categoryServices) {
        this.categoryServices = categoryServices;
    }
    @GetMapping
    public List<Category> getAllBook()
    {
        return categoryServices.getAllCategory();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id){
        Category cate = categoryServices.getCategoryById(id);
        return ResponseEntity.ok(cate);
    }
    @PostMapping("/add")
    public ResponseEntity<MessageResponse> createCategory(@RequestBody Category category){
        if(categoryServices.isCategoryExit(category)){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("category is exit"));
        }
        Category saveCategory =  categoryServices.saveCategory(category);

        return ResponseEntity.ok(new MessageResponse("Ok"));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category category)
    {
        Optional<Category> categoryUpdate = Optional.ofNullable(categoryServices.getCategoryById(id));
        return  categoryUpdate.map(category1->{
            category.setId(category.getId());
            return new ResponseEntity<>(categoryServices.saveCategory(category),HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Category> deleteCategory(@PathVariable Long id)
    {
        Category deleteCategory = categoryServices.getCategoryById(id);
        categoryServices.deleteCategory(id);
        return ResponseEntity.ok(deleteCategory);
    }
}
