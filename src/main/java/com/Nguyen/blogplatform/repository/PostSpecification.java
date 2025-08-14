package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Post;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PostSpecification {
//    public static Specification<Post> hasTitle(String title) {
//        return (root, query, criteriaBuilder) ->
//                title == null ? criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("title"), "%" + title + "%");
//    }
//
//    public static Specification<Post> hasCategoryId(Long categoryId) {
//        return (root, query, criteriaBuilder) ->
//                categoryId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("categoryId"), categoryId);
//    }

    public static Specification<Post> hasTitle(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(root.get("title"), "%" + title + "%");
        };
    }

    public static Specification<Post> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.join("categories").get("id"), categoryId);
        };
    }

    public static Specification<Post> hasTagId(UUID tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.join("tags").get("uuid"), tagId);
        };
    }

    public static Specification<Post> hasCategorySlug(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.join("categories").get("slug"), categorySlug);
        };
    }

    public static Specification<Post> hasTagSlug(String tagSlug) {
        return (root, query, cb) -> {
            if (tagSlug == null || tagSlug.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.join("tags").get("slug"), tagSlug);
        };
    }
}
